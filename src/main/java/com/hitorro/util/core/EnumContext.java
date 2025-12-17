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
package com.hitorro.util.core;

import gnu.trove.map.hash.TIntObjectHashMap;
import com.hitorro.util.core.string.StringUtil;

import java.lang.reflect.Array;
import java.util.HashMap;

/**
 * Utility class to make enum lookups a little easier to handle by giving name lookups
 */
public class EnumContext<E extends Enum> {
    private HashMap<String, Enum> byShortName;
    private HashMap<String, Enum> byEnumName;
    private TIntObjectHashMap<Enum> ordinalsMap;

    private String name;
    private Class enumClass;

    public EnumContext(String name) {
        this.name = name;
        byShortName = new HashMap<>();
        byEnumName = new HashMap<>();
        ordinalsMap = new TIntObjectHashMap();
    }

    public int size() {
        return byShortName.size();
    }

    public void setNames(E e, String shortName, int ordinal) {
        byShortName.put(shortName.toLowerCase(), e);
        byEnumName.put(e.name().toLowerCase(), e);
        this.enumClass = e.getDeclaringClass();
        ordinalsMap.put(ordinal, e);
    }

    public void setNames(E e, String shortName) {
        byShortName.put(shortName.toLowerCase(), e);
        byEnumName.put(e.name().toLowerCase(), e);
        this.enumClass = e.getDeclaringClass();
        ordinalsMap.put(e.ordinal(), e);
    }

    public String getName() {
        return name;
    }

    public E getByShortName(String name) {
        return (E) byShortName.get(name.toLowerCase());
    }

    public E getByOrdinal(int ordinal) {
        return (E) ordinalsMap.get(ordinal);
    }

    public E getByEnumName(String name) {
        return (E) byEnumName.get(name.toLowerCase());
    }

    private E getByNameAux(String name, boolean shortName) {
        if (shortName) {
            return (E) byEnumName.get(name.toLowerCase());
        }
        return (E) byShortName.get(name.toLowerCase());
    }


    public E[] getEnumsFromShortNames(String names) {
        return getEnumsFromNamesAux(names, false);
    }

    public E[] getEnumsFromEnumNames(String names) {
        return getEnumsFromNamesAux(names, true);
    }

    private E[] getEnumsFromNamesAux(String names, boolean shortNames) {
        String parts[] = StringUtil.tokenizeFromSingleChar(names, ",", true);
        if (ArrayUtil.nullOrEmpty(parts)) {
            return null;
        }
        // see if we start enums from 0th position (if tokenizer is implied)
        E e = getByNameAux(parts[0], shortNames);

        if (e == null) {
            E filters[] = (E[]) Array.newInstance(enumClass, parts.length - 1);
            for (int i = 1; i <= filters.length; i++) {
                e = getByNameAux(parts[i], shortNames);
                if (e == null) {
                    return null;
                }
                filters[i - 1] = e;
            }

            return filters;
        }
        E filters[] = (E[]) Array.newInstance(enumClass, parts.length);
        filters[0] = e;
        for (int i = 1; i < filters.length; i++) {
            e = getByNameAux(parts[i], shortNames);
            if (e == null) {
                return null;
            }
            filters[i] = e;
        }
        return filters;
    }
}
