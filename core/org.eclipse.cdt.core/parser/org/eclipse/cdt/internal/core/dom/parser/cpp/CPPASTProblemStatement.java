/*******************************************************************************
 * Copyright (c) 2004, 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM - Initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *     Sergey Prigogin (Google)
 *     Thomas Corbat (IFS)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTAttribute;
import org.eclipse.cdt.core.dom.ast.IASTAttributeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTProblem;
import org.eclipse.cdt.core.dom.ast.IASTProblemStatement;

/**
 * @author jcamelon
 */
public class CPPASTProblemStatement extends CPPASTProblemOwner implements IASTProblemStatement {
	
    public CPPASTProblemStatement() {
		super();
	}

	public CPPASTProblemStatement(IASTProblem problem) {
		super(problem);
	}

	@Override
	public CPPASTProblemStatement copy() {
		return copy(CopyStyle.withoutLocations);
	}

	@Override
	public CPPASTProblemStatement copy(CopyStyle style) {
		CPPASTProblemStatement copy = new CPPASTProblemStatement();
		return copy(copy, style);
	}

	@Override
	public boolean accept(ASTVisitor action) {
        if (action.shouldVisitStatements) {
		    switch (action.visit(this)) {
	            case ASTVisitor.PROCESS_ABORT: return false;
	            case ASTVisitor.PROCESS_SKIP: return true;
	            default: break;
	        }
		}
        super.accept(action);	// visits the problem
        if (action.shouldVisitStatements) {
		    switch (action.leave(this)) {
	            case ASTVisitor.PROCESS_ABORT: return false;
	            case ASTVisitor.PROCESS_SKIP: return true;
	            default: break;
	        }
		}
        return true;
    }

	@Override
	public IASTAttribute[] getAttributes() {
		return IASTAttribute.EMPTY_ATTRIBUTE_ARRAY;
	}

	@Override
	public void addAttribute(IASTAttribute attribute) {
		assertNotFrozen();
		// Ignore.
	}

	@Override
	public IASTAttributeSpecifier[] getAttributeSpecifiers() {
		return IASTAttributeSpecifier.EMPTY_ATTRIBUTE_SPECIFIER_ARRAY;
	}

	@Override
	public void addAttributeSpecifier(IASTAttributeSpecifier attributeSpecifier) {
		assertNotFrozen();
		// Ignore.
	}
}
