/*******************************************************************************
 * Copyright (c) 2004, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM - Initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTProblem;
import org.eclipse.cdt.core.dom.ast.IASTProblemHolder;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;

/**
 * @author jcamelon
 */
abstract class CPPASTProblemOwner extends ASTNode implements IASTProblemHolder {
    private IASTProblem problem;

    public CPPASTProblemOwner() {
	}

	public CPPASTProblemOwner(IASTProblem problem) {
		setProblem(problem);
	}

	protected <T extends CPPASTProblemOwner> T copy(T copy, CopyStyle style) {
		copy.setProblem(problem == null ? null : problem.copy(style));
		return super.copy(copy, style);
	}

	@Override
	public IASTProblem getProblem() {
        return problem;
    }

    @Override
	public void setProblem(IASTProblem p) {
        assertNotFrozen();
        problem = p;
        if (p != null) {
			p.setParent(this);
			p.setPropertyInParent(PROBLEM);
		}
    }

    @Override
	public boolean accept(ASTVisitor action) {
        if (action.shouldVisitProblems) {
		    switch (action.visit(getProblem())) {
	            case ASTVisitor.PROCESS_ABORT: return false;
	            case ASTVisitor.PROCESS_SKIP: return true;
	            default: break;
	        }
		    switch (action.leave(getProblem())) {
	            case ASTVisitor.PROCESS_ABORT: return false;
	            case ASTVisitor.PROCESS_SKIP: return true;
	            default: break;
	        }
		}
        return true;
    }
}
