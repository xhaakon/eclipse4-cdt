/*******************************************************************************
 * Copyright (c) 2007, 2012 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 * Raphael Zulliger (Indel AG) - bug 284699: fixing issues when using same
 *                               macro names with different values
 *******************************************************************************/
package org.eclipse.cdt.core.settings.model.util;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

import org.eclipse.cdt.core.settings.model.ICExclusionPatternPathEntry;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.core.settings.model.ICSettingEntry;
import org.eclipse.core.runtime.IPath;

/**
 * Cache of setting entries stored with multidimentional maps.
 * See bug 284699 comment 7 for a possible insight into it.
 */
public class CSettingEntryFactory {
	private static final HashSet<IPath> EMPTY_SET = new HashSet<IPath>(0);

	private KindBasedStore<HashMap<String, ?>> fStore = new KindBasedStore<HashMap<String, ?>>(false);

	private <K, V> HashMap<String, HashMap<K, V>> getNameMap(int kind, boolean create){
		@SuppressWarnings("unchecked")
		HashMap<String/*name*/, HashMap<K, V>> map = (HashMap<String, HashMap<K, V>>) fStore.get(kind);
		if(map == null && create){
			map = new HashMap<String, HashMap<K, V>>();
			fStore.put(kind, map);
		}
		return map;
	}

	private HashMap<String, HashMap<Integer, ICSettingEntry>> getValueMap(String name, boolean create){
		HashMap<String/*name*/, HashMap<String/*value*/, HashMap<Integer/*flags*/, ICSettingEntry>>> nameMap = getNameMap(ICSettingEntry.MACRO, create);
		if(nameMap != null){
			return getMap(nameMap, name, create);
		}
		return null;
	}

	private HashMap<Integer, ICSettingEntry> getFlagMap(int kind, String name, String value, IPath[] exclusionPatterns, boolean create){
		switch(kind){
		case ICSettingEntry.MACRO:
			HashMap<String, HashMap<Integer, ICSettingEntry>> valueMap = getValueMap(name, create);
			if(valueMap != null){
				return getMap(valueMap, value, create);
			}
			return null;
		case ICSettingEntry.SOURCE_PATH:
		case ICSettingEntry.OUTPUT_PATH:
			HashMap<HashSet<IPath>, HashMap<Integer, ICSettingEntry>> excPatternMap = getExclusionPatternsMap(kind, name, create);
			if(excPatternMap != null){
				HashSet<IPath> setKey = exclusionPatterns == null || exclusionPatterns.length == 0 ? EMPTY_SET : new HashSet<IPath>(Arrays.asList(exclusionPatterns));
				return getMap(excPatternMap, setKey, create);
			}
			return null;
		default:
			HashMap<String, HashMap<Integer, ICSettingEntry>> nameMap = getNameMap(kind, create);
			if(nameMap != null){
				return getMap(nameMap, name, create);
			}
			return null;
		}
	}

	private HashMap<HashSet<IPath>, HashMap<Integer, ICSettingEntry>> getExclusionPatternsMap(int kind, String name, boolean create){
		HashMap<String/*name*/, HashMap<HashSet<IPath>/*exclusionPatterns*/, HashMap<Integer/*flags*/, ICSettingEntry>>> nameMap = getNameMap(kind, create);
		if(nameMap != null){
			return getMap(nameMap, name, create);
		}
		return null;
	}

	private static <Key, K, V> HashMap<K, V> getMap(HashMap<Key, HashMap<K, V>> container, Key key, boolean create){
		HashMap<K, V> map = container.get(key);
		if(map == null && create){
			map = new HashMap<K, V>();
			container.put(key, map);
		}
		return map;
	}

	public ICSettingEntry getEntry(ICSettingEntry entry){
		switch(entry.getKind()){
		case ICSettingEntry.OUTPUT_PATH:
		case ICSettingEntry.SOURCE_PATH:
			return getEntry(entry.getKind(), entry.getName(), null, ((ICExclusionPatternPathEntry)entry).getExclusionPatterns(), entry.getFlags(), entry, true);
		default:
			return getLanguageSettingEntry((ICLanguageSettingEntry)entry);
		}
	}

	public ICLanguageSettingEntry getLanguageSettingEntry(ICLanguageSettingEntry lEntry){
		return (ICLanguageSettingEntry)getEntry(lEntry.getKind(), lEntry.getName(), lEntry.getValue(), null, lEntry.getFlags(), lEntry, true);
	}

	public ICSettingEntry getEntry(int kind, String name, String value, IPath[] exclusionPatterns, int flags, boolean create){
		return getEntry(kind, name, value, exclusionPatterns, flags, null, create);
	}

	private ICSettingEntry getEntry(int kind, String name, String value, IPath[] exclusionPatterns, int flags, ICSettingEntry baseEntry, boolean create){
		HashMap<Integer, ICSettingEntry> flagMap = getFlagMap(kind, name, value, exclusionPatterns, create);
		if(flagMap != null){
			Integer iFlags = new Integer(flags);
			ICSettingEntry entry = flagMap.get(iFlags);
			if(entry == null && create){
				entry = baseEntry != null ? baseEntry : CDataUtil.createEntry(kind, name, value, exclusionPatterns, flags);
				flagMap.put(iFlags, entry);
			}
			return entry;
		}
		return null;
	}

	public void clear(){
		fStore.clear();
	}
}
