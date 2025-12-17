package ht.util.core.valuemap;

import java.util.HashMap;

/**
 * Copyright (c) 2003 - present HiTorro All rights reserved. User: chris Date: Nov 3, 2006 Time: 12:36:23 PM
 */
public class DomainValueMap<E> {
    private HashMap<String, ValueMap<E>> m_map = new HashMap<String, ValueMap<E>>();

    public ValueMap<E> getValueMap(String domain) {
        return m_map.get(domain);
    }

    public void addValueMap(ValueMap<E> vm, String domain) {
        m_map.put(domain, vm);
    }
}
