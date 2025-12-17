package ht.util.core.valuemap;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Copyright (c) 2003 - present HiTorro All rights reserved. User: chris Date: Nov 3, 2006 Time: 11:51:45 AM
 */
public class FlatValueMap<E> implements ValueMap<E> {
    protected String m_domain;
    protected HashMap<String, E> m_map = new HashMap<String, E>();

    public void setDomain(String domain) {
        m_domain = domain;
    }

    public Set<String> getKeys() {
        return m_map.keySet();
    }

    public Collection<E> getValues() {
        return m_map.values();
    }

    public Set<Map.Entry<String, E>> getEntrySet() {
        return m_map.entrySet();
    }

    public boolean isHierarchical() {
        return false;
    }

    public boolean isUniqueOverSystemVersions() {
        return false;
    }

    public E getValueNonDefaulting(String key) {
        return m_map.get(key);
    }

    public E getValue(String key) {
        return getValueNonDefaulting(key);
    }

    public E setValue(E value, String key) {
        return m_map.put(key, value);
    }

    public boolean validate(String key) {
        return getValueNonDefaulting(key) != null;
    }
}
