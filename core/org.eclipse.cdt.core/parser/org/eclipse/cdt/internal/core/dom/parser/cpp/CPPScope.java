/*******************************************************************************
 * Copyright (c) 2004, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrew Niefer (IBM Corporation) - initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *     Bryan Wilkinson (QNX)
 *     Andrew Ferguson (Symbian)
 *     Jens Elmenthaler - http://bugs.eclipse.org/173458 (camel case completion)
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.IName;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamespaceDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateId;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespace;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespaceScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPUsingDeclaration;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexBinding;
import org.eclipse.cdt.core.index.IIndexFileSet;
import org.eclipse.cdt.core.index.IndexFilter;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.core.parser.util.CharArrayObjectMap;
import org.eclipse.cdt.core.parser.util.IContentAssistMatcher;
import org.eclipse.cdt.core.parser.util.ObjectSet;
import org.eclipse.cdt.internal.core.dom.parser.ProblemBinding;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPSemantics;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVisitor;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.LookupData;
import org.eclipse.cdt.internal.core.parser.util.ContentAssistMatcherFactory;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;

/**
 * Base class for c++-scopes of the AST.
 */
abstract public class CPPScope implements ICPPASTInternalScope {
	protected static final char[] CONSTRUCTOR_KEY = "!!!CTOR!!!".toCharArray(); //$NON-NLS-1$
	private static final IProgressMonitor NPM = new NullProgressMonitor();
	private static final ICPPNamespace UNINITIALIZED = new CPPNamespace.CPPNamespaceProblem(null, 0, null);

    private IASTNode physicalNode;
	private boolean isCached = false;
	protected CharArrayObjectMap<Object> bindings;
	private ICPPNamespace fIndexNamespace= UNINITIALIZED;

	public static class CPPScopeProblem extends ProblemBinding implements ICPPScope {
        public CPPScopeProblem(IASTNode node, int id, char[] arg) {
            super(node, id, arg);
        }

        public CPPScopeProblem(IASTName name, int id) {
            super(name, id);
        }
    }

	public CPPScope(IASTNode physicalNode) {
		this.physicalNode = physicalNode;
	}

	@Override
	public IScope getParent() throws DOMException {
		return CPPVisitor.getContainingNonTemplateScope(physicalNode);
	}

	@Override
	public IASTNode getPhysicalNode() {
		return physicalNode;
	}

	@Override
	@SuppressWarnings({ "unchecked" })
	public void addName(IASTName name) {
		// Don't add inactive names to the scope.
		if (!name.isActive())
			return;

		if (bindings == null)
			bindings = new CharArrayObjectMap<Object>(1);
		if (name instanceof ICPPASTQualifiedName &&
				!(physicalNode instanceof ICPPASTCompositeTypeSpecifier) &&
				!(physicalNode instanceof ICPPASTNamespaceDefinition)) {
			return;
		}

		final char[] c= name.getLookupKey();
		if (c.length == 0)
			return;
		Object o = bindings.get(c);
		if (o != null) {
		    if (o instanceof ObjectSet) {
		    	((ObjectSet<Object>) o).put(name);
		    } else {
		    	ObjectSet<Object> temp = new ObjectSet<Object>(2);
		    	temp.put(o);
		    	temp.put(name);
		        bindings.put(c, temp);
		    }
		} else {
		    bindings.put(c, name);
		}
	}

	@Override
	public IBinding getBinding(IASTName name, boolean forceResolve, IIndexFileSet fileSet) {
		IBinding binding= getBindingInAST(name, forceResolve);
		if (binding == null && forceResolve) {
			final IASTTranslationUnit tu = name.getTranslationUnit();
			IIndex index = tu == null ? null : tu.getIndex();
			if (index != null) {
				final char[] nchars = name.getLookupKey();
				// Try looking this up in the index.
				if (physicalNode instanceof IASTTranslationUnit) {
					try {
						IBinding[] bindings= index.findBindings(nchars,
								IndexFilter.CPP_DECLARED_OR_IMPLICIT_NO_INSTANCE, NPM);
						if (fileSet != null) {
							bindings= fileSet.filterFileLocalBindings(bindings);
						}
						binding= CPPSemantics.resolveAmbiguities(name, bindings);
			        	if (binding instanceof ICPPUsingDeclaration) {
			        		binding= CPPSemantics.resolveAmbiguities(name,
			        				((ICPPUsingDeclaration) binding).getDelegates());
			        	}
					} catch (CoreException e) {
		        		CCorePlugin.log(e);
					}
				} else {
					ICPPNamespace nsbinding= getNamespaceIndexBinding(index);
					if (nsbinding != null) {
						return nsbinding.getNamespaceScope().getBinding(name, forceResolve, fileSet);
					}
				}
			}
		}
		return binding;
	}

	protected ICPPNamespace getNamespaceIndexBinding(IIndex index) {
		if (fIndexNamespace == UNINITIALIZED) {
			fIndexNamespace= null;
			IASTNode node= getPhysicalNode();
			if (node instanceof ICPPASTNamespaceDefinition) {
				IASTName nsname = ((ICPPASTNamespaceDefinition) node).getName();
				IBinding nsbinding= nsname.resolveBinding();
				if (nsbinding != null) {
					fIndexNamespace= (ICPPNamespace) index.adaptBinding(nsbinding);
				}
			}
		}
		return fIndexNamespace;
	}

	public IBinding getBindingInAST(IASTName name, boolean forceResolve) {
		IBinding[] bs= getBindingsInAST(name, forceResolve, false, false);
		return CPPSemantics.resolveAmbiguities(name, bs);
	}

	@Override
	public final IBinding[] getBindings(IASTName name, boolean resolve, boolean prefixLookup, IIndexFileSet fileSet) {
		return getBindings(name, resolve, prefixLookup, fileSet, true);
	}

	@Override
	public IBinding[] getBindings(IASTName name, boolean resolve, boolean prefixLookup, IIndexFileSet fileSet,
			boolean checkPointOfDecl) {
		IBinding[] result = getBindingsInAST(name, resolve, prefixLookup, checkPointOfDecl);
		final IASTTranslationUnit tu = name.getTranslationUnit();
		if (tu != null) {
			IIndex index = tu.getIndex();
			if (index != null) {
				if (physicalNode instanceof IASTTranslationUnit) {
					try {
						IndexFilter filter = IndexFilter.CPP_DECLARED_OR_IMPLICIT_NO_INSTANCE;
						final char[] nchars = name.getLookupKey();
						IBinding[] bindings = prefixLookup ?
								index.findBindingsForContentAssist(nchars, true, filter, null) :
								index.findBindings(nchars, filter, null);
						if (fileSet != null) {
							bindings= fileSet.filterFileLocalBindings(bindings);
						}
						result = ArrayUtil.addAll(IBinding.class, result, bindings);
					} catch (CoreException e) {
						CCorePlugin.log(e);
					}
				} else if (physicalNode instanceof ICPPASTNamespaceDefinition) {
					ICPPASTNamespaceDefinition ns = (ICPPASTNamespaceDefinition) physicalNode;
					try {
						IIndexBinding binding = index.findBinding(ns.getName());
						if (binding instanceof ICPPNamespace) {
							ICPPNamespaceScope indexNs = ((ICPPNamespace) binding).getNamespaceScope();
							IBinding[] bindings = indexNs.getBindings(name, resolve, prefixLookup);
							if (fileSet != null) {
								bindings= fileSet.filterFileLocalBindings(bindings);
							}
							result = ArrayUtil.addAll(IBinding.class, result, bindings);
						}
					} catch (CoreException e) {
						CCorePlugin.log(e);
					}
				}
			}
		}

		return ArrayUtil.trim(IBinding.class, result);
	}


	public IBinding[] getBindingsInAST(IASTName name, boolean forceResolve, boolean prefixLookup,
			boolean checkPointOfDecl) {
		populateCache();
	    final char[] c = name.getLookupKey();
	    IBinding[] result = null;

	    Object obj = null;
	    if (prefixLookup) {
	    	Object[] keys = bindings != null ? bindings.keyArray() : new Object[0];
	    	ObjectSet<Object> all= new ObjectSet<Object>(16);
	    	IContentAssistMatcher matcher = ContentAssistMatcherFactory.getInstance().createMatcher(c);
	    	for (Object key2 : keys) {
	    		final char[] key = (char[]) key2;
				if (key != CONSTRUCTOR_KEY && matcher.match(key)) {
	    			obj= bindings.get(key);
	    			if (obj instanceof ObjectSet<?>) {
	    				all.addAll((ObjectSet<?>) obj);
	    			} else if (obj != null) {
	    				all.put(obj);
	    			}
	    		}
	    	}
	    	obj= all;
	    } else {
	    	obj = bindings != null ? bindings.get(c) : null;
	    }

	    if (obj != null) {
	        if (obj instanceof ObjectSet<?>) {
	        	ObjectSet<?> os= (ObjectSet<?>) obj;
        		for (int j = 0; j < os.size(); j++) {
        			result= addCandidate(os.keyAt(j), name, forceResolve, checkPointOfDecl, result);
        		}
	        } else {
	        	result = addCandidate(obj, name, forceResolve, checkPointOfDecl, result);
	        }
	    }
	    return ArrayUtil.trim(IBinding.class, result);
	}

	private IBinding[] addCandidate(Object candidate, IASTName name, boolean forceResolve,
			boolean checkPointOfDecl, IBinding[] result) {
		if (checkPointOfDecl) {
			IASTTranslationUnit tu= name.getTranslationUnit();
			if (!CPPSemantics.declaredBefore(candidate, name, tu != null && tu.getIndex() != null)) {
				if (!(this instanceof ICPPClassScope) || !LookupData.checkWholeClassScope(name))
					return result;
			}
		}

		IBinding binding;
		if (candidate instanceof IASTName) {
			final IASTName candName= (IASTName) candidate;
			IASTName simpleName= candName.getLastName();
			if (simpleName instanceof ICPPASTTemplateId) {
				simpleName= ((ICPPASTTemplateId) simpleName).getTemplateName();
			}
			if (forceResolve && candName != name && simpleName != name) {
				candName.resolvePreBinding();  // Make sure to resolve the template-id
				binding = simpleName.resolvePreBinding();
			} else {
				binding = simpleName.getPreBinding();
			}
		} else {
			binding= (IBinding) candidate;
		}

		return ArrayUtil.append(IBinding.class, result, binding);
	}

	@Override
	public final void populateCache() {
		if (!isCached) {
			CPPSemantics.populateCache(this);
			isCached= true;
		}
	}

	@Override
	public void removeNestedFromCache(IASTNode container) {
		if (bindings != null) {
			removeFromMap(bindings, container);
		}
	}

	private void removeFromMap(CharArrayObjectMap<Object> map, IASTNode container) {
		for (int i = 0; i < map.size(); i++) {
			Object o= map.getAt(i);
			if (o instanceof IASTName) {
				if (container.contains((IASTNode) o)) {
					final char[] key = map.keyAt(i);
					map.remove(key, 0, key.length);
					i--;
				}
			} else if (o instanceof ObjectSet) {
				@SuppressWarnings("unchecked")
				final ObjectSet<Object> set = (ObjectSet<Object>) o;
				removeFromSet(set, container);
			}
		}
	}

	private void removeFromSet(ObjectSet<Object> set, IASTNode container) {
		for (int i = 0; i < set.size(); i++) {
			Object o= set.keyAt(i);
			if (o instanceof IASTName) {
				if (container.contains((IASTNode) o)) {
					set.remove(o);
					i--;
				}
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IScope#find(java.lang.String)
	 */
	@Override
	public IBinding[] find(String name) {
	    return CPPSemantics.findBindings(this, name, false);
	}

	@Override
	@SuppressWarnings({ "unchecked" })
    public void addBinding(IBinding binding) {
        if (bindings == null)
            bindings = new CharArrayObjectMap<Object>(1);
        char[] c = binding.getNameCharArray();
        if (c.length == 0) {
        	return;
        }
        Object o = bindings.get(c);
        if (o != null) {
            if (o instanceof ObjectSet) {
                ((ObjectSet<Object>) o).put(binding);
            } else {
                ObjectSet<Object> set = new ObjectSet<Object>(2);
                set.put(o);
                set.put(binding);
                bindings.put(c, set);
            }
        } else {
            bindings.put(c, binding);
        }
    }

	@Override
	public final IBinding getBinding(IASTName name, boolean resolve) {
		return getBinding(name, resolve, IIndexFileSet.EMPTY);
	}

	@Override
	public final IBinding[] getBindings(IASTName name, boolean resolve, boolean prefix) {
		return getBindings(name, resolve, prefix, IIndexFileSet.EMPTY, true);
	}

	@Override
	public IName getScopeName() {
		return null;
	}

	@Override
	public String toString() {
		IName name = getScopeName();
		final String n= name != null ? name.toString() : "<unnamed scope>"; //$NON-NLS-1$
		return getKind().toString() + ' ' + n + ' ' + '(' + super.toString() + ')';
	}
}
