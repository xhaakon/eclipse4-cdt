/*******************************************************************************
 * Copyright (c) 2012, 2013 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Markus Schorn - initial API and implementation
 *     Sergey Prigogin (Google) 
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp.semantics;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.internal.core.dom.parser.ISerializableEvaluation;
import org.eclipse.cdt.internal.core.dom.parser.ISerializableType;
import org.eclipse.cdt.internal.core.dom.parser.ITypeMarshalBuffer;
import org.eclipse.cdt.internal.core.dom.parser.ProblemType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPEvaluation;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPUnknownType;
import org.eclipse.core.runtime.CoreException;

/**
 * Represents the type of a dependent expression.
 */
public class TypeOfDependentExpression implements ICPPUnknownType, ISerializableType {
	private final ICPPEvaluation fEvaluation;

	public TypeOfDependentExpression(ICPPEvaluation evaluation) {
		fEvaluation= evaluation;
	}
	
	public ICPPEvaluation getEvaluation() {
		return fEvaluation;
	}

	@Override
	public boolean isSameType(IType type) {
		return type instanceof TypeOfDependentExpression
				&& fEvaluation == ((TypeOfDependentExpression) type).fEvaluation;
	}
	
	@Override
	public Object clone() {
		try {
			return super.clone();
		} catch (CloneNotSupportedException e) {
			return null;
		}
	}

	public char[] getSignature() {
		SignatureBuilder buf = new SignatureBuilder();
		try {
			marshal(buf);
		} catch (CoreException e) {
			CCorePlugin.log(e);
			return new char[] { '?' };
		}
		return buf.getSignature();
	}

	@Override
	public void marshal(ITypeMarshalBuffer buffer) throws CoreException {
		buffer.putByte(ITypeMarshalBuffer.DEPENDENT_EXPRESSION_TYPE);
		buffer.marshalEvaluation(fEvaluation, false);
	}
	
	public static IType unmarshal(int firstByte, ITypeMarshalBuffer buffer) throws CoreException {
		ISerializableEvaluation eval= buffer.unmarshalEvaluation();
		if (eval instanceof ICPPEvaluation)
			return new TypeOfDependentExpression((ICPPEvaluation) eval);
		return ProblemType.UNKNOWN_FOR_EXPRESSION;
	}
}
