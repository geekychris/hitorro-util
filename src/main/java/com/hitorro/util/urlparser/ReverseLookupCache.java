/*
 * Copyright (c) 2006-2025 Chris Collins
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.hitorro.util.urlparser;

import com.hitorro.util.core.map.LRUHashMap;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**

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
