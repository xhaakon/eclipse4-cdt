/*******************************************************************************
 * Copyright (c) 2014 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Marc Khouzam (Ericsson) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.core.breakpoints;

import java.util.Map;

import org.eclipse.cdt.debug.core.CDebugUtils;
import org.eclipse.cdt.debug.core.model.ICBreakpoint;
import org.eclipse.cdt.debug.core.model.ICDynamicPrintf;
import org.eclipse.cdt.debug.core.model.ICLineBreakpoint2;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

import com.ibm.icu.text.MessageFormat;

/**
 * A DynamicPrintf that prints a string when a particular line of code is reached.
 *
 * @since 7.5
 */
public class CLineDynamicPrintf extends AbstractDynamicPrintf implements ICDynamicPrintf, ICLineBreakpoint2 {

	public CLineDynamicPrintf() {
	}

	public CLineDynamicPrintf( IResource resource, Map<String, Object> attributes, boolean add ) throws CoreException {
		super( resource, attributes, add );
	}

	@Override
	public String getMarkerType() {
	    return C_LINE_DYNAMICPRINTF_MARKER;
	}
	
	/*(non-Javadoc)
	 * @see org.eclipse.cdt.debug.internal.core.breakpoints.CBreakpoint#getMarkerMessage()
	 */
	@Override
	protected String getMarkerMessage() throws CoreException {
		IMarker marker = this.getMarker();
		int bp_line = 0;
		int bp_request_line = 0;
		String bp_file = null;
		String bp_reqest_file = null;
		
		if (marker != null) {
			bp_line = marker.getAttribute(IMarker.LINE_NUMBER, -1);
			bp_request_line = marker.getAttribute(ICLineBreakpoint2.REQUESTED_LINE, -1);
			bp_file = marker.getAttribute(ICBreakpoint.SOURCE_HANDLE, (String)null);
            bp_reqest_file = marker.getAttribute(ICLineBreakpoint2.REQUESTED_SOURCE_HANDLE, (String)null);
		}
		
		if (bp_line != bp_request_line || 
		    (bp_file == null && bp_reqest_file != null) ||  
		    (bp_file != null && !bp_file.equals(bp_reqest_file)) ) 
		{
			return MessageFormat.format( BreakpointMessages.getString( "CLineDynamicPrintf.1" ), (Object[])new String[] { CDebugUtils.getBreakpointText( this, false ) } ); //$NON-NLS-1$			
		}
		else {
			return MessageFormat.format( BreakpointMessages.getString( "CLineDynamicPrintf.0" ), (Object[])new String[] { CDebugUtils.getBreakpointText( this, false ) } ); //$NON-NLS-1$

		}
	}	
}
