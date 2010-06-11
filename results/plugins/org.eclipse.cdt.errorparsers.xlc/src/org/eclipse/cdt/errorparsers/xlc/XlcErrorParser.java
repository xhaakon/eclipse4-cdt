/*******************************************************************************
 * Copyright (c) 2006, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Andrew Gvozdev (Quoin Inc.)
 *******************************************************************************/

package org.eclipse.cdt.errorparsers.xlc;


/**
 * This class provides for parsing the error messages generated by IBM AIX xlC compiler.
 *
 */

import java.util.regex.Matcher;

import org.eclipse.cdt.core.IMarkerGenerator;
import org.eclipse.cdt.core.errorparsers.AbstractErrorParser;
import org.eclipse.cdt.core.errorparsers.ErrorPattern;

/**
 * Class XlcErrorParser provides for parsing of error output messages
 * produced by AIX xlC compiler and linker.
 *
 * @see org.eclipse.cdt.core.errorparsers.AbstractErrorParser
 * @see org.eclipse.cdt.core.errorparsers.ErrorPattern
*/
public class XlcErrorParser extends AbstractErrorParser {
	private static final ErrorPattern[] patterns = {
		/**
		 * xlC produces 2 warning messages in case of macro redefinition:
		 * "hello.c", line 3.9: 1506-236 (W) Macro name HELLO has been redefined.
		 * "hello.c", line 3.9: 1506-358 (I) "HELLO" is defined on line 4 of hello.h.
		 * Both can be captured by regular XlcErrorParser_CompilerErrorPattern. However
		 * different severity puts them apart in different groups. In addition
		 * the second entry in Problems view won't let you jump to the original definition.
		 * This ErrorPattern fixes those shortcomings.
		 */
		new ErrorPattern(Messages.XlcErrorParser_MacroRedefinitionErrorPattern, 6, 5, -1, 0, -1) {
			/*
			 * @see org.eclipse.cdt.core.errorparsers.ErrorPattern#getSeverity(java.util.regex.Matcher)
			 */
			@Override
			public int getSeverity(Matcher matcher) {
				return IMarkerGenerator.SEVERITY_WARNING;
			}
			/*
			 * @see org.eclipse.cdt.core.errorparsers.ErrorPattern#getDesc(java.util.regex.Matcher)
			 */
			@Override
			public String getDesc(Matcher matcher) {
				return " Macro name " + matcher.group(4) + " originally defined in file " + getFileName(matcher);
			}
		},
		new ErrorPattern(Messages.XlcErrorParser_CompilerErrorPattern, 1, 2, 5, 0, -1) {
			/*
			 * @see org.eclipse.cdt.core.errorparsers.ErrorPattern#getSeverity(java.util.regex.Matcher)
			 */
			@Override
			public int getSeverity(Matcher matcher) {
				String warningGroup = matcher.group(4);
				if (warningGroup != null) {
					if (warningGroup.equals(Messages.XlcErrorParser_FlagUnrecoverable)) {
						return IMarkerGenerator.SEVERITY_ERROR_BUILD;
					} else if (warningGroup.equals(Messages.XlcErrorParser_FlagSevere)
							|| warningGroup.equals(Messages.XlcErrorParser_FlagError)) {
						return IMarkerGenerator.SEVERITY_ERROR_RESOURCE;
					} else if (warningGroup.equals(Messages.XlcErrorParser_FlagWarning)) {
						return IMarkerGenerator.SEVERITY_WARNING;
					} else if (warningGroup.equals(Messages.XlcErrorParser_FlagInfo)) {
						return IMarkerGenerator.SEVERITY_INFO;
					}
				}
				return IMarkerGenerator.SEVERITY_INFO;
			}
		},
		new ErrorPattern(Messages.XlcErrorParser_LinkerErrorPattern, 0, 0, 3, 0, -1) {
			/*
			 * @see org.eclipse.cdt.core.errorparsers.ErrorPattern#getSeverity(java.util.regex.Matcher)
			 */
			@Override
			public int getSeverity(Matcher matcher) {
				String warningGroup = matcher.group(2);
				if (warningGroup != null) {
					if (warningGroup.indexOf(Messages.XlcErrorParser_LinkerWarning) >= 0) {
						return IMarkerGenerator.SEVERITY_WARNING;
					} else if (warningGroup.indexOf(Messages.XlcErrorParser_LinkerError) >= 0) {
						return IMarkerGenerator.SEVERITY_ERROR_RESOURCE;
					}
				}
				return IMarkerGenerator.SEVERITY_INFO;
			}
		},
		new ErrorPattern(Messages.XlcErrorParser_LinkerErrorPattern2, 0, 0, 2, 0, -1) {
			/*
			 * @see org.eclipse.cdt.core.errorparsers.ErrorPattern#getSeverity(java.util.regex.Matcher)
			 */
			@Override
			public int getSeverity(Matcher matcher) {
				return IMarkerGenerator.SEVERITY_ERROR_RESOURCE;
			}
		},
		new ErrorPattern(Messages.XlcErrorParser_LinkerInfoPattern, 0, 0, 2, 0, -1) {
			/*
			 * @see org.eclipse.cdt.core.errorparsers.ErrorPattern#getSeverity(java.util.regex.Matcher)
			 */
			@Override
			public int getSeverity(Matcher matcher) {
				return IMarkerGenerator.SEVERITY_INFO;
			}
		},
	};

	/**
	 * The constructor.
	 */
	public XlcErrorParser() {
		super(patterns);
	}

}