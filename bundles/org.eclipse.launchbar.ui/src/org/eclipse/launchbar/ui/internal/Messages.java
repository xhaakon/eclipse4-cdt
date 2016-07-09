/*******************************************************************************
 * Copyright (c) 2014, 2016 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.launchbar.ui.internal;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.launchbar.ui.internal.messages"; //$NON-NLS-1$

	public static String BuildActiveCommandHandler_0;
	public static String BuildActiveCommandHandler_1;
	public static String StopActiveCommandHandler_0;
	public static String StopActiveCommandHandler_1;

	public static String NewLaunchConfigEditPage_0;
	public static String NewLaunchConfigEditPage_1;
	public static String NewLaunchConfigEditPage_2;
	public static String NewLaunchConfigEditPage_3;
	public static String NewLaunchConfigEditPage_4;
	public static String NewLaunchConfigEditPage_5;
	public static String NewLaunchConfigEditPage_6;
	public static String NewLaunchConfigEditPage_7;
	public static String NewLaunchConfigModePage_0;
	public static String NewLaunchConfigModePage_1;
	public static String NewLaunchConfigModePage_2;
	public static String NewLaunchConfigTypePage_0;
	public static String NewLaunchConfigTypePage_1;
	public static String NewLaunchConfigTypePage_2;
	public static String NewLaunchConfigWizard_0;

	public static String DescriptorMustNotBeNull;
	public static String DescriptorMustNotBeNullDesc;
	public static String NoActiveTarget;
	public static String NoActiveTargetDesc;
	public static String NoLaunchConfigType;
	public static String CannotEditLaunchConfiguration;
	public static String NoLaunchModeSelected;
	public static String NoLaunchGroupSelected;
	public static String LaunchConfigurationNotFound;
	public static String LaunchConfigurationNotFoundDesc;
	public static String NoLaunchTabsDefined;
	public static String NoLaunchTabsDefinedDesc;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
