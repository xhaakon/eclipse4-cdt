/*******************************************************************************
 * Copyright (c) 2009, 2010 Andrew Gvozdev
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrew Gvozdev  - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.internal.checkers.ui.quickfix;

import org.eclipse.cdt.codan.internal.checkers.ui.CheckersUiActivator;
import org.eclipse.cdt.codan.internal.checkers.ui.Messages;
import org.eclipse.cdt.codan.ui.AbstractCodanCMarkerResolution;
import org.eclipse.core.resources.IMarker;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.FindReplaceDocumentAdapter;
import org.eclipse.jface.text.IDocument;

/**
 * quick fix for assignment in condition
 */
public class QuickFixAssignmentInCondition extends AbstractCodanCMarkerResolution {
	@Override
	public String getLabel() {
		return Messages.QuickFixAssignmentInCondition_Message;
	}

	@Override
	public void apply(IMarker marker, IDocument document) {
		int pos = getOffset(marker, document);
		try {
			FindReplaceDocumentAdapter dad = new FindReplaceDocumentAdapter(document);
			dad.find(pos, "=", /* forwardSearch *///$NON-NLS-1$
					true, /* caseSensitive */false,
					/* wholeWord */false, /* regExSearch */false);
			dad.replace("==", /* regExReplace */false); //$NON-NLS-1$
		} catch (BadLocationException e) {
			CheckersUiActivator.log(e);
		}
	}
}
