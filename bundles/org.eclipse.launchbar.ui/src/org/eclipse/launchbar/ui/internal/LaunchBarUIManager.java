/*******************************************************************************
 * Copyright (c) 2014 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Doug Schaefer
 *******************************************************************************/
package org.eclipse.launchbar.ui.internal;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchMode;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.launchConfigurations.LaunchConfigurationPresentationManager;
import org.eclipse.debug.internal.ui.launchConfigurations.LaunchGroupExtension;
import org.eclipse.debug.ui.ILaunchConfigurationTabGroup;
import org.eclipse.debug.ui.ILaunchGroup;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.launchbar.core.ILaunchBarManager;
import org.eclipse.launchbar.core.ILaunchDescriptor;
import org.eclipse.launchbar.core.internal.ExecutableExtension;
import org.eclipse.launchbar.core.target.ILaunchTarget;
import org.eclipse.launchbar.ui.ILaunchBarUIManager;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

public class LaunchBarUIManager implements ILaunchBarUIManager {

	private Map<String, ExecutableExtension<ILabelProvider>> descriptorLabelProviders = null;
	private ILaunchBarManager manager = Activator.getService(ILaunchBarManager.class);

	private void init() {
		if (descriptorLabelProviders == null) {
			descriptorLabelProviders = new HashMap<>();
			IExtensionPoint point = Platform.getExtensionRegistry().getExtensionPoint(Activator.PLUGIN_ID, "launchBarUIContributions"); //$NON-NLS-1$
			IExtension[] extensions = point.getExtensions();
			for (IExtension extension : extensions) {
				for (IConfigurationElement element : extension.getConfigurationElements()) {
					String elementName = element.getName();
					if (elementName.equals("descriptorUI")) { //$NON-NLS-1$
						String descriptorTypeId = element.getAttribute("descriptorTypeId"); //$NON-NLS-1$
						ExecutableExtension<ILabelProvider> labelProvider = new ExecutableExtension<>(element, "labelProvider"); //$NON-NLS-1$
						descriptorLabelProviders.put(descriptorTypeId, labelProvider);
					}
				}
			}
		}
	}

	@Override
	public ILabelProvider getLabelProvider(ILaunchDescriptor descriptor) throws CoreException {
		init();
		ExecutableExtension<ILabelProvider> provider = descriptorLabelProviders.get(manager.getDescriptorTypeId(descriptor.getType()));
		return provider != null ? provider.get() : null;
	}

	@Override
	public IStatus openConfigurationEditor(ILaunchDescriptor descriptor) {
		if (descriptor == null)
			return Status.OK_STATUS;

		// Display the error message
		Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
		IStatus s = canOpenConfigurationEditor(descriptor);
		if (!s.isOK()) {
			MessageDialog.openError(shell, s.getMessage(),
					s.getException() == null ? s.getMessage() : s.getException().getMessage());
			return s;
		}

		// At this point, no error handling should be needed.
		try {
			ILaunchBarManager manager = Activator.getService(ILaunchBarManager.class);
			ILaunchMode mode = manager.getActiveLaunchMode();
			ILaunchTarget target = manager.getActiveLaunchTarget();
			ILaunchConfigurationType configType = manager.getLaunchConfigurationType(descriptor, target);
			ILaunchGroup group = DebugUIPlugin.getDefault().getLaunchConfigurationManager().getLaunchGroup(configType,
					mode.getIdentifier());
			ILaunchConfiguration config = manager.getLaunchConfiguration(descriptor, target);
			if (config instanceof ILaunchConfigurationWorkingCopy
					&& ((ILaunchConfigurationWorkingCopy) config).isDirty()) {
				config = ((ILaunchConfigurationWorkingCopy) config).doSave();
			}
			// open real eclipse launch configurations dialog
			DebugUIPlugin.openLaunchConfigurationsDialog(shell, new StructuredSelection(config),
					group.getIdentifier(), false);
			return Status.OK_STATUS;
		} catch (CoreException e2) {
			return e2.getStatus();
		}
	}

	private IStatus canOpenConfigurationEditor(ILaunchDescriptor desc) {
		if (desc == null)
			return new Status(IStatus.ERROR, Activator.PLUGIN_ID, Messages.DescriptorMustNotBeNull,
					new Exception(Messages.DescriptorMustNotBeNullDesc));
		ILaunchBarManager manager = Activator.getService(ILaunchBarManager.class);
		ILaunchMode mode = null;
		ILaunchTarget target = null;
		try {
			mode = manager.getActiveLaunchMode();
			target = manager.getActiveLaunchTarget();
		} catch (CoreException e) {
			return e.getStatus();
		}
		if (target == null) {
			return new Status(IStatus.ERROR, Activator.PLUGIN_ID, Messages.NoActiveTarget,
					new Exception(Messages.NoActiveTargetDesc));
		}

		ILaunchConfigurationType configType = null;
		try {
			configType = manager.getLaunchConfigurationType(desc, target);
		} catch (CoreException ce) {
			return ce.getStatus();
		}

		if (mode == null) {
			return new Status(IStatus.ERROR, Activator.PLUGIN_ID, Messages.NoLaunchModeSelected,
					new Exception(Messages.NoLaunchModeSelected));
		}

		ILaunchGroup group = DebugUIPlugin.getDefault().getLaunchConfigurationManager().getLaunchGroup(configType,
				mode.getIdentifier());
		if (group == null) {
			return new Status(IStatus.ERROR, Activator.PLUGIN_ID, Messages.NoLaunchGroupSelected,
					new Exception(Messages.NoLaunchGroupSelected));
		}

		String mode2 = group.getMode();
		if (mode2 == null) {
			return new Status(IStatus.ERROR, Activator.PLUGIN_ID, Messages.NoLaunchModeSelected,
					new Exception(Messages.CannotEditLaunchConfiguration));
		}

		LaunchGroupExtension groupExt = DebugUIPlugin.getDefault().getLaunchConfigurationManager()
				.getLaunchGroup(group.getIdentifier());
		if (groupExt != null) {
			ILaunchConfiguration config = null;
			try {
				config = manager.getLaunchConfiguration(desc, target);
			} catch (CoreException ce) {
				// Ignore
			}
			if (config == null) {
				return new Status(IStatus.ERROR, Activator.PLUGIN_ID, Messages.LaunchConfigurationNotFound,
						new Exception(Messages.LaunchConfigurationNotFoundDesc));
			}
			try {
				LaunchConfigurationPresentationManager mgr = LaunchConfigurationPresentationManager.getDefault();
				ILaunchConfigurationTabGroup tabgroup = mgr.getTabGroup(config, mode.getIdentifier());
			} catch (CoreException ce) {
				return new Status(IStatus.ERROR, Activator.PLUGIN_ID, Messages.NoLaunchTabsDefined,
						new Exception(Messages.NoLaunchTabsDefinedDesc));
			}
		} else {
			return new Status(IStatus.ERROR, Activator.PLUGIN_ID, Messages.CannotEditLaunchConfiguration,
					new Exception(Messages.CannotEditLaunchConfiguration));
		}
		return Status.OK_STATUS;
	}

}
