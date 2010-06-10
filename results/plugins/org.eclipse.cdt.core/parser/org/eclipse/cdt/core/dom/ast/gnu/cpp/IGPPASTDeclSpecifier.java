/*******************************************************************************
 * Copyright (c) 2004, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    John Camelon (IBM) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast.gnu.cpp;

import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;

/**
 * G++ allows for restrict to be a modifier for the decl spec.
 * 
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IGPPASTDeclSpecifier extends IASTDeclSpecifier {

	/**
	 * Was restrict keyword encountered?
	 * 
	 * @return boolean
	 */
	public boolean isRestrict();

	/**
	 * Set restrict-modifier-encountered to value.
	 * 
	 * @param value
	 *            boolean
	 */
	public void setRestrict(boolean value);

	/**
	 * @since 5.1
	 */
	public IGPPASTDeclSpecifier copy();
}
