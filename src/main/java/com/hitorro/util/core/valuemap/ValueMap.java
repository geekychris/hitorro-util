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
import java.util.Map;
import java.util.Set;

/**
 */
public interface ValueMap<E> {

    void setDomain(String domain);

    /**
     * The domain is hierarchically oriented.
     *
     * @return
     */
    boolean isHierarchical();


    Set<Map.Entry<String, E>> getEntrySet();

    Set<String> getKeys();

    Collection<E> getValues();

    /**
     * If this value is applied to a versionableobject category, it must be unique over all system versions.  This is
     * specific for "labels" such as "release"
     *
     * @return
     */
    boolean isUniqueOverSystemVersions();

    /**
     * Get the value or null if not found.  If this is a hierarchical mechanism, do not traverse up the tree if a
     * matching value can not be found.
     *
     * @param key
     * @return
     */
    E getValueNonDefaulting(String key);

    /**
     * get a value and if there is no matching value and this is a hierarchical apply and one knows how to manipulate the
     * key, traverse up the apply. to find the best test.
     *
     * @param key to search for a value for.
     * @return Value if found
     */
    E getValue(String key);


    /**
     * put a value to the domain
     *
     * @param value
     * @param key
     * @return
     */
    E setValue(E value, String key);

    /**
     * Determine if this key is valid.  In some domains, we are not managing the values such as with labels.
     *
     * @param key to validate
     * @return true if valid
     */
    boolean validate(String key);

}
