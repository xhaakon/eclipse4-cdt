/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Rational Software - Initial API and implementation 
 *******************************************************************************/
package org.eclipse.cdt.ui.tests.DOMAST;

import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorIncludeStatement;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

/**
 * @author dsteffle
 */
public class IncludeStatementFilter extends ViewerFilter {

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ViewerFilter#select(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
	 */
	public boolean select(Viewer viewer, Object parentElement, Object element) {
		if (element instanceof DOMASTNodeLeaf) {
			int flag = ((DOMASTNodeLeaf)element).getFiltersFlag() & DOMASTNodeLeaf.FLAG_INCLUDE_STATEMENTS;
			if (flag > 0) {
				IASTNode node = ((DOMASTNodeLeaf)element).getNode();
				if (node instanceof IASTPreprocessorIncludeStatement)
					return false;
				
				return true;
			}
		}

		return true;
	}

}
