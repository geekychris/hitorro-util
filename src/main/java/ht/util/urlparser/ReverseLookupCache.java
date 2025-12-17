package ht.util.urlparser;

import ht.util.core.map.LRUHashMap;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Copyright (c) 2003 - present HiTorro All rights reserved.
 * <p/>
 * User: chris
 * <p/>
 * Cache of reverse looked up ip addresses to names.  Will return the ip address if it could not be resolved.
 */
public class ReverseLookupCache {
    private static final String NullFlyWeight = "NullFlyWeight";
    private LRUHashMap<String, String> m_map;

    public ReverseLookupCache(int size) {
        m_map = new LRUHashMap(size);
    }


    public String lookup(String ip) {
        String result = m_map.get(ip);
        if (result != null) {
            if (result == NullFlyWeight) {
                return null;
            }
            return result;
        }
        try {
            result = lookupAux(ip);
            m_map.put(ip, result);
            return result;
        } catch (UnknownHostException e) {
            result = NullFlyWeight;
            m_map.put(ip, result);
            return null;
        }
    }

    private String lookupAux(String ip) throws UnknownHostException {
        InetAddress addr = InetAddress.getByName(ip);
        String name = addr.getHostName();
        return name;
    }
}
