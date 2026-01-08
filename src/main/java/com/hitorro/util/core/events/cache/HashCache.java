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
import com.hitorro.util.core.string.Fmt;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p/>
 * Set of key->value pairs optimized for O(n) time lookup.  Missing values can be fetched by the subclass that
 * implements a getAux method.
 * <p/>
 * This set supports storing a null value in the set to avoid constant attempts to recompute.
 */
public class HashCache<K, V> extends Cache<K, V> {
    protected Map<Object, V> m_table = new HashMap<Object, V>();
    private boolean m_demandBasedCacheing = true;
    private V m_flyweight = null;

    private Mapper<K, V> mapper;

    public HashCache(Mapper<K, V> mapper) {
        this(0, true, null, null, mapper);
    }

    public HashCache(String eventName,
                     Mapper<K, V> mapper) {
        this(0, true, null, eventName, mapper);
    }

    public HashCache(String eventName,
                     V nullFlyweight,
                     Mapper<K, V> mapper) {
        this(0, true, nullFlyweight, eventName, mapper);
    }

    /**
     * Constructor
     *
     * @param demandBasedCacheing if set to false, then all put, updates' get recognized as operations that need to be
     *                            performed. If set to true, then deletes delete and updates delete immediately.
     * @param nullFlyweight       not null if you want a null result from a fetch to not perform the getAux again
     */
    public HashCache(long refreshInterval,
                     boolean demandBasedCacheing,
                     V nullFlyweight,
                     String eventName,
                     Mapper<K, V> mapper) {
        super(refreshInterval, eventName);
        m_demandBasedCacheing = demandBasedCacheing;
        m_flyweight = nullFlyweight;
        this.mapper = mapper;
    }

    protected HashCache() {
    }

    /**
     * Convenience Method to construct HashCache for a pool
     *
     * @param name
     * @param maxValues
     * @param mapper
     * @param <K>
     * @param <V>
     * @return
     */
    public static <K, V extends PooledObjectIntf> HashCache<K, PoolContainer<K, V>> getPooledCache(String name, int maxValues, Mapper<K, V> mapper) {
        return new HashCache(0, true, null, name, new PooledObjectContainerMapper(maxValues, mapper));
    }

    public String getDescription() {
        return Fmt.S("HashCache for %s using apply %s", eventName(), mapper.getClass().getName());
    }

    public V get(K key) {
        synchronized (m_table) {
            if (this.considerCacheFlush()) {
                flushCache();
                this.resetConsiderClearingCache();
            }
            V result = m_table.get(key);
            if (result != null) {
                if (m_flyweight != null && result == m_flyweight) {
                    // flyweight  null
                    return null;
                }
                return result;
            }
            return getAndCache(key);
        }
    }

    protected V getAndCache(K key) {
        V result = getAux(key);
        if (result != null) {
            // store result
            m_table.put(key, result);
            return result;
        }
        if (m_flyweight != null) {
            m_table.put(key, m_flyweight);
        }
        return null;
    }

    /**
     * This operation DOES NOT GUARANTEE that the keys will be added to the cache.  They are simply messaged to the
     * cache that they could be cached.
     *
     * @param keys of items that could be added to the cache
     */
    protected void addToCache(Object keys) {
        if (!m_demandBasedCacheing) {
            if (keys instanceof List) {
                List l = (List) keys;
                synchronized (m_table) {
                    // only do this if we are not demand based.
                    int size = l.size();
                    for (int i = 0; i < size; i++) {
                        getAndCache((K) l.get(i));
                    }
                }
            }

        }
    }

    /**
     * This operation DOES NOT GUARANTEE that the keys will be updated to the cache.  They are simply messaged to the
     * cache that they could be cached.
     *
     * @param l of items that could be added to the cache
     */
    protected void updateCache(Object l) {

        synchronized (m_table) {
            if (l instanceof List) {
                List keys = (List) l;
                int size = keys.size();
                if (m_demandBasedCacheing) {
                    // just remove

                    for (int i = 0; i < size; i++) {
                        removeItem(keys.get(i));
                    }
                } else {
                    // we are going to remove and put
                    for (int i = 0; i < size; i++) {
                        removeItem(keys.get(i));
                        getAndCache((K) keys.get(i));
                    }
                }
            }
        }
    }

    /**
     * This operation DOES NOT GUARANTEE that the keys will be deleted to the cache.  They are simply messaged to the
     * cache that they could be cached.
     *
     * @param l of items that could be deleted to the cache
     */
    protected void deleteFromCache(Object l) {
        synchronized (m_table) {
            if (l instanceof List) {
                List keys = (List) l;
                int size = keys.size();
                for (int i = 0; i < size; i++) {
                    removeItem(keys.get(i));
                }
            }
        }
    }

    protected void removeItem(Object key) {
        m_table.remove(key);
    }

    /**
     * @param key
     * @return
     */
    protected V getAux(K key) {
        return mapper.apply(key);
    }

    protected void flushCache() {
        m_table.clear();
    }

    public void flushCacheBit() {
        super.flushCacheBit();
        flushAllRelated();
    }
}
