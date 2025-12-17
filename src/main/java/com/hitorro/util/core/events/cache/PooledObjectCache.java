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

import com.hitorro.util.core.events.WeakReferenceList;
import com.hitorro.util.core.iterator.Mapper;
import com.hitorro.util.core.iterator.mappers.BaseMapper;

import java.util.ArrayList;
import java.util.List;

public class PooledObjectCache<K, V extends PooledObjectIntf> {
    private static PoolContainer nullElem = new PoolContainer(null, 0, null, 0);
    private static WeakReferenceList<PooledObjectCache> pool = new WeakReferenceList<PooledObjectCache>();
    protected int maxElements;
    private Mapper<K, V> mapper;
    private PoolMapper poolMapper;
    private HashCache<K, PoolContainer<K, V>> hashCache;

    public PooledObjectCache(int maxElements, boolean demandBasedCacheing,
                             String eventName, Mapper<K, V> mapper) {
        this.mapper = mapper;
        poolMapper = new PoolMapper(this, maxElements);
        hashCache = new HashCache<K, PoolContainer<K, V>>(0, demandBasedCacheing, nullElem, eventName, poolMapper);
        this.maxElements = maxElements;
        PooledObjectCache.addPoolToWatchList(this);
    }

    public static void addPoolToWatchList(PooledObjectCache poolItem) {
        synchronized (pool) {
            pool.add(poolItem);
        }
    }

    public static List<PooledObjectCache> getWatchedPools() {
        synchronized (pool) {
            ArrayList<PooledObjectCache> l = new ArrayList();
            for (int i = 0; i < pool.size(); i++) {
                l.add(pool.get(i));
            }
            return l;
        }
    }

    public void returnIt(K key, V value) {
        PoolContainer<K, V> pc = hashCache.get(key);
        pc.returnIt(value);
    }

    public V get(K key) {
        PoolContainer<K, V> pc = hashCache.get(key);
        if (pc == null) {
            return null;
        }
        return pc.get();
    }

    /**
     * The thing that gives us a
     *
     * @param key
     * @return
     */
    protected V getValue(K key) {
        return mapper.apply(key);
    }
}

class PoolMapper<K, V extends PooledObjectIntf> extends BaseMapper<K, PoolContainer<K, V>> {
    private PooledObjectCache cache;
    private int generation = 0;
    private int maxElements = 0;

    public PoolMapper(PooledObjectCache cache, int maxElements) {
        this.cache = cache;
        this.maxElements = maxElements;
    }

    public PoolContainer<K, V> apply(K key) {
        return new PoolContainer(key, maxElements, cache, generation++);
    }
}





