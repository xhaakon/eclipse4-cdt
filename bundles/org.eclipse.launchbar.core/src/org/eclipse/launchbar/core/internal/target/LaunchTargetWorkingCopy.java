/*******************************************************************************
 * Copyright (c) 2016 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.launchbar.core.internal.target;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.launchbar.core.internal.Activator;
import org.eclipse.launchbar.core.target.ILaunchTarget;
import org.eclipse.launchbar.core.target.ILaunchTargetManager;
import org.eclipse.launchbar.core.target.ILaunchTargetWorkingCopy;
import org.osgi.service.prefs.BackingStoreException;

public class LaunchTargetWorkingCopy extends PlatformObject implements ILaunchTargetWorkingCopy {

	private final LaunchTarget original;
	private final Map<String, String> changes = new HashMap<>();
	private String newId;

	public LaunchTargetWorkingCopy(LaunchTarget original) {
		this.original = original;
	}

	@Override
	public ILaunchTarget getOriginal() {
		return original;
	}

	@Override
	public String getTypeId() {
		return original.getTypeId();
	}

	@Override
	public String getId() {
		return newId != null ? newId : original.getId();
	}

	@Override
	public void setId(String id) {
		newId = id;
	}

	@Override
	public ILaunchTargetWorkingCopy getWorkingCopy() {
		return this;
	}

	@Override
	public String getAttribute(String key, String defValue) {
		if (changes.containsKey(key)) {
			return changes.get(key);
		} else {
			return original.getAttribute(key, defValue);
		}
	}

	@Override
	public void setAttribute(String key, String value) {
		changes.put(key, value);
	}

	@Override
	public ILaunchTarget save() {
		try {
			LaunchTarget target;
			if (newId == null) {
				target = original;
			} else {
				// make a new one and remove the old one
				ILaunchTargetManager manager = Activator.getLaunchTargetManager();
				target = (LaunchTarget) manager.addLaunchTarget(original.getTypeId(), newId);
				for (String key : original.attributes.keys()) {
					target.attributes.put(key, original.getAttribute(key, "")); //$NON-NLS-1$
				}
				manager.removeLaunchTarget(original);
			}
			
			// set the changed attributes
			for (Map.Entry<String, String> entry : changes.entrySet()) {
				target.attributes.put(entry.getKey(), entry.getValue());
			}
			target.attributes.flush();
			return target;
		} catch (BackingStoreException e) {
			Activator.log(e);
			return original;
		}
	}

}
