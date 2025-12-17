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
package com.hitorro.util.core.longword;


import gnu.trove.map.hash.TLongObjectHashMap;
import gnu.trove.map.hash.TObjectLongHashMap;

/**
 *
 */
public class BiDirectionalKeyLongMap {
    private TObjectLongHashMap<String> map = new TObjectLongHashMap();
    private TLongObjectHashMap<String> reverse = new TLongObjectHashMap();
    private boolean ignoreCase;

    public BiDirectionalKeyLongMap(boolean ignoreCase) {
        this.ignoreCase = ignoreCase;
    }

    public String mapToString(final long l) {
        return reverse.get(l);
    }

    public long mapFromString(final String e) {
        Long v = null;
        if (ignoreCase) {
            v = map.get(e.toLowerCase());
        } else {
            v = map.get(e);
        }
        if (v == null) {
            return Long.parseLong(e);
        }
        return v;
    }

    public void add(String key, long val) {
        if (ignoreCase) {
            map.put(key.toLowerCase(), val);
            reverse.put(val, key);
        } else {
            map.put(key, val);
            reverse.put(val, key);
        }
    }
}
