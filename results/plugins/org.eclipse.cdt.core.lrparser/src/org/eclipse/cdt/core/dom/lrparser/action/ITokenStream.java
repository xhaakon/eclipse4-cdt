/*******************************************************************************
 * Copyright (c) 2006, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom.lrparser.action;

import java.util.List;

import lpg.lpgjavaruntime.IToken;

/**
 * Provides an interface to the token stream that
 * can be used by the parser semantic actions.
 * 
 * Allows the semantic actions to directly inspect the token 
 * stream. Used to calculate AST node offsets and for 
 * other purposes.
 * 
 * TODO There are still issues with getLeftIToken() and 
 * getRightIToken(), they should return null when used with
 * an empty rule but currently they don't.
 * 
 * @author Mike Kucera
 */
public interface ITokenStream {
	
	/**
	 * Returns the tokens that were parsed to recognized
	 * the currently executing rule.
	 * 
	 * @returns a read-only list of tokens, will not be null but may be empty
	 */
	public List<IToken> getRuleTokens();
	
	/**
	 * Usually equivalent to getRuleTokens().get(0); but more efficient.
	 * 
	 * However, when called during an empty rule it will return the token to the
	 * left of the location of the empty rule.
	 */
	public IToken getLeftIToken();
	
	/**
	 * Usually equivalent to getRuleTokens().get(getRuleTokens().size()-1); but more efficient.
	 * 
	 * However, when called during an empty rule it will return the token to the
	 * right of the location of the empty rule.
	 */
	public IToken getRightIToken(); 
	

	/**
	 * Returns the orderedTerminalSymbol field of the corresponding sym class
	 * generated by LPG.
	 */
	public String[] getOrderedTerminalSymbols();
	
	
	/**
	 * Returns the parser's name, useful for debugging.
	 */
	public String getName();
}
