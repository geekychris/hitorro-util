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

import com.hitorro.util.core.diff.GenericDiffer;
import com.hitorro.util.core.diff.GenericDifferContainer;

import java.util.*;

public class ListUtil {
    public static <E> List<GenericKeyValue<String, E>> addGKVWithDefaultValue(List<String> keys,
                                                                              E value,
                                                                              List<GenericKeyValue<String, E>> gkvs) {
        for (String key : keys) {
            gkvs.add(new GenericKeyValue<>(key, value));
        }
        return gkvs;
    }

    public static <E> List<GenericKeyValue<String, E>> addGKVWithDefaultValue(String keys[],
                                                                              E value,
                                                                              List<GenericKeyValue<String, E>> gkvs) {
        for (String key : keys) {
            gkvs.add(new GenericKeyValue<>(key, value));
        }
        return gkvs;
    }

    public static final boolean addIfAbsent(List target, Object o, Comparator c) {
        for (Object e : target) {
            if (c.compare(e, o) == 0) {
                return false;
            }
        }
        target.add(o);
        return true;
    }

    public static final void remove(List target, Object o, Comparator c) {
        for (int i = target.size() - 1; i >= 0; i--) {
            Object e = target.get(i);
            if (c.compare(e, o) == 0) {
                target.remove(i);
            }
        }
    }

    public static final <E, F> boolean addAllValuesFromMap(Map<E, F> sourceMap, List<F> targetList) {
        if (sourceMap.size() == 0) {
            return false;
        }
        targetList.addAll(sourceMap.values());
        return true;
    }

    public static final boolean equal(List old, List newL) {
        if (old.size() != newL.size()) {
            return false;
        }
        for (int i = 0; i < old.size(); i++) {
            if (!old.get(i).equals(newL.get(i))) {
                return false;
            }
        }
        return true;
    }

    public static final <T> void notNullAdd(List<T> list, T elem) {
        if (elem != null) {
            list.add(elem);
        }
    }

    /**
     * Add any rows from the source that are missing in the target.
     *
     * @param target
     * @param source
     */
    public static final void addAllAbsent(List target, List source) {
        for (Object o : source) {
            if (!target.contains(o)) {
                target.add(o);
            }
        }
    }

    public static final void addIfAbsent(List target, Object o) {
        if (o != null && !target.contains(o)) {
            target.add(o);
        }
    }

    public static final <E> List<E> getIntersection(List<E> a, List<E> b, Comparator<E> c) {
        GenericDifferContainer<E> cont = new GenericDifferContainer<E>();
        GenericDiffer<E> differ = new GenericDiffer<E>();
        differ.diff(a.iterator(), b.iterator(), c, cont);
        return cont.getModified();
    }

    public static final List iteratorToList(Iterator iter, List list) {
        while (iter.hasNext()) {
            list.add(iter.next());
        }

        return list;
    }

    /**
     * Create a standard, untyped, List.
     *
     * @return a new List
     */
    public static List list() {
        return new ArrayList();
    }

    public static final boolean notNullAndContainsRows(List l) {
        return l != null && l.size() > 0;
    }

    /**
     * Detect if a listFiles is null or empty
     *
     * @param l
     * @return
     */
    public static final boolean nullOrEmpty(List l) {
        return l == null || l.size() == 0;
    }

    /**
     * Prune the tail end of a listFiles away to the length defined in maxLength
     *
     * @param list
     * @param maxLength
     */
    public static final void pruneListToLength(List list, int maxLength) {
        int startIndex;
        int itemsToRemove = list.size() - maxLength;
        if (itemsToRemove > 0) {
            startIndex = list.size() - 1;
            for (int i = startIndex; itemsToRemove > 0; i--) {
                list.remove(i);
                itemsToRemove--;
            }
        }
    }

    /**
     * Remove duplicate items from a listFiles.  First it sorts the listFiles by the comparitor.
     *
     * @param l
     * @param c
     * @return
     */
    public static final boolean removeDuplicates(List l, Comparator c) {
        Collections.sort(l, c);

        int size = l.size();
        for (int i = size - 1; i > 0; i--) {
            if (c.compare(l.get(i), l.get(i - 1)) == 0) {
                // remove the lower one.
                l.remove(i);
            }
        }
        return true;
    }
}
