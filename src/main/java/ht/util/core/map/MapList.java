package ht.util.core.map;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Copyright (c) 2003 - present HiTorro All rights reserved. User: chris Date: Oct 16, 2006 Time: 1:20:30 PM
 * <p/>
 * Simple wrapper around a Map, to allow us to keep a listFiles of x to a key.
 */
public class MapList<KEY, LISTTYPE> {
    private Map<KEY, List<LISTTYPE>> m_map = new HashMap<KEY, List<LISTTYPE>>();

    public List<LISTTYPE> get(KEY key) {
        return m_map.get(key);
    }

    public void add(KEY key, LISTTYPE element) {
        List<LISTTYPE> l = get(key);
        if (l == null) {
            l = new ArrayList<LISTTYPE>();
            m_map.put(key, l);
        }
        l.add(element);
    }
}
