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
package com.hitorro.util.core.events.cache;

import com.hitorro.util.core.iterator.Mapper;

/**
 * Pool of nxK pooled element.  Meaning for anything of type K we can have up to n elements pooled. The implementor must
 * subclass the pooledobject class to profide a method that will create an object of type V that implements a
 * PooledObjectIntf.  This interface is used to allow general activate pasivate logic and to allow determination if the
 * pooled item being returned should be returned or thrown away if the cache has been purged in the meantime (returning
 * an object rendered by a prior version of the cache could mean that its data is inacurate. The user of the cache does
 * not handle K objects directly, they provide a key to the get method that returns a PoolContainer for that key.  For
 * instance the cache could contain localized strings for a given language.  K could be the given language. The pool
 * returned would be a bag of instances of that localization.
 * <p/>
 * The PoolContainer is demand based in its expansion.  Initially it contains 0 elements and expands to a max n.  Users
 * of the pool ask for elements of type V and once used can return those elements.  When asking for an element and the
 * pool has reached n the caller will block until another caller returns an object to that pool
 */
public class PooledObjectContainerMapper<K, V extends PooledObjectIntf> implements Mapper<K, PoolContainer<K, V>> {
    protected int maxElements;
    private int generation = 1;
    private Mapper<K, V> mapper;

    public PooledObjectContainerMapper(int maxElements, Mapper<K, V> mapper) {
        this.maxElements = maxElements;
        this.mapper = mapper;
    }

    @Override
    public PoolContainer<K, V> apply(final K key) {
        String eventName = "pool";
        PooledObjectCache poc = new PooledObjectCache(maxElements, true, eventName, mapper);
        return new PoolContainer(key, maxElements, poc, generation++);
    }
}






