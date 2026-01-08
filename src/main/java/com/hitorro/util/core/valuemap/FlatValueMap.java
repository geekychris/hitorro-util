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
package com.hitorro.util.core.valuemap;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
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
