package ht.util.core.map;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * Copyright (c) 2003 - present HiTorro All rights reserved. User: chris Date: Dec 1, 2006 Time: 1:41:46 PM wrapper to
 * allow pass through of the getter operation....we have created a two tier hashmap.
 * <p/>
 * WARNING: the keysets, collections etc DO NOT FUNCTION CORRECTLY.  THIS SHOULD PRIMARILY BE USED FOR get!
 */
public class PassThroughMap<K, V> implements Map<K, V> {
    private Map<K, V> m_top;
    private Map<K, V> m_bottom;

    public PassThroughMap(Map<K, V> top, Map<K, V> bottom) {
        m_top = top;
        m_bottom = bottom;
    }

    public int size() {
        return m_top.size() + m_bottom.size();
    }

    public boolean isEmpty() {
        return m_top.isEmpty() && m_bottom.isEmpty();
    }

    public boolean containsKey(Object o) {
        return m_top.containsKey(o) || m_bottom.containsKey(o);
    }

    public boolean containsValue(Object o) {
        return m_top.containsValue(o) || m_bottom.containsValue(o);
    }

    public V get(Object o) {
        V val = m_top.get(o);
        if (val != null) {
            return val;
        }
        return m_bottom.get(o);
    }

    public V put(K k, V v) {
        return m_top.put(k, v);
    }

    public V remove(Object o) {
        return m_top.remove(o);
    }

    public void putAll(Map<? extends K, ? extends V> map) {
        m_top.putAll(map);
    }

    public void clear() {
        m_top.clear();
    }

    public Set<K> keySet() {
        return m_top.keySet();
    }

    public Collection<V> values() {
        return m_top.values();
    }

    public Set<Entry<K, V>> entrySet() {
        return m_top.entrySet();
    }
}
