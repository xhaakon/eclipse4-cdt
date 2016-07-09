/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.launchbar.ui.target;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.launchbar.core.target.ILaunchTarget;
import org.eclipse.ui.wizards.IWizardDescriptor;

/**
 * A manager for the launch target UI.
 */
public interface ILaunchTargetUIManager {

	/**
	 * Return a label provider that gives the test and image for the target.
	 *
	 * @param target
	 *            the launch target
	 * @return the label provider for the launch target
	 */
	ILabelProvider getLabelProvider(ILaunchTarget target);

	public IWizardDescriptor[] getLaunchTargetWizards();
}
