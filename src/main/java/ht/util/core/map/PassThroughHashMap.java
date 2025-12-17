package ht.util.core.map;

import java.util.HashMap;
import java.util.Map;

/**
 * Copyright (c) 2003 - present HiTorro All rights reserved. User: chris Date: Dec 1, 2006 Time: 1:00:45 PM
 * <p/>
 * Map that on the getter if this apply contains nothing, it deligates to a super
 */
public class PassThroughHashMap<K, V> extends HashMap<K, V> {
    private Map<K, V> m_superMap;

    public PassThroughHashMap(Map<K, V> superMap) {
        m_superMap = superMap;
    }

    public V get(Object k) {
        V v = super.get(k);
        if (v != null) {
            return v;
        }
        return m_superMap.get(k);
    }
}
