package ht.util.core.events.cache;

import ht.util.core.events.WeakReferenceList;

/**
 * /** Copyright (c) 2003 - present HiTorro All rights reserved. User: chris Date: Oct 17, 2006 Time: 6:39:16 PM
 */
public class CacheRegistry {
    private static CacheRegistry me = new CacheRegistry();

    private WeakReferenceList<Cache> elems = new WeakReferenceList();

    private WeakReferenceList<CacheRelation> relations = new WeakReferenceList();

    public static CacheRegistry getMe() {
        return me;
    }

    public void register(Cache c) {
        elems.add(c);
    }

    public void registerRelation(CacheRelation cr) {
        relations.add(cr);
    }
}
