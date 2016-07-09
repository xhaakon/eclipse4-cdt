/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.launchbar.core.internal.target;

import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.launchbar.core.target.ILaunchTarget;
import org.eclipse.launchbar.core.target.ILaunchTargetWorkingCopy;
import org.osgi.service.prefs.Preferences;

public class LaunchTarget extends PlatformObject implements ILaunchTarget {
	public static final ILaunchTarget NULL_TARGET = new LaunchTarget("null", "---") {
		@Override
		public ILaunchTargetWorkingCopy getWorkingCopy() {
			throw new UnsupportedOperationException("getWorkingCopy is not supported for NULL_TARGET");
		};
	};
	private final String typeId;
	private final String id;
	final Preferences attributes;

	/**
	 * This should only be used to create the null target. There are no attributes supported on the
	 * null target.
	 */
	private LaunchTarget(String typeId, String id) {
		this.typeId = typeId;
		this.id = id;
		this.attributes = null;
	}

	public LaunchTarget(String typeId, String id, Preferences attributes) {
		if (typeId == null || id == null || attributes == null)
			throw new NullPointerException();
		this.typeId = typeId;
		this.id = id;
		this.attributes = attributes;
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public String getTypeId() {
		return typeId;
	}

	@Override
	public ILaunchTargetWorkingCopy getWorkingCopy() {
		return new LaunchTargetWorkingCopy(this);
	}

	@Override
	public String getAttribute(String key, String defValue) {
		if (attributes != null) {
			return attributes.get(key, defValue);
		} else {
			return defValue;
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id.hashCode();
		result = prime * result + typeId.hashCode();
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		LaunchTarget other = (LaunchTarget) obj;
		if (!id.equals(other.id))
			return false;
		if (!typeId.equals(other.typeId))
			return false;
		return true;
	}
}
