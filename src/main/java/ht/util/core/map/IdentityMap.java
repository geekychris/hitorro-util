package ht.util.core.map;


import gnu.trove.map.hash.TObjectByteHashMap;

/**
 * Copyright (c) 2003 - present HiTorro All rights reserved. User: chris Date: Feb 27, 2005 Time: 4:17:10 PM
 * <p/>
 * Simple container of a hash table that allows you to keep track of who exists.
 */
public class IdentityMap<E> {
    private TObjectByteHashMap m_map = new TObjectByteHashMap();

    /**
     * @param key
     * @return true if already in the apply
     */
    public boolean addIfAbsent(E key) {
        if (m_map.containsKey(key)) {
            return true;
        }
        m_map.put(key, (byte) 1);
        return false;
    }

    public boolean contains(E key) {
        return m_map.containsKey(key) == true;
    }
}
