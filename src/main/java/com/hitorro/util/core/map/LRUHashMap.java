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
package com.hitorro.util.core.map;


import java.util.*;

interface LRURemovalCallback<VAL> {
    /**
     * This method gets called to notify a cache entry that it has been aged out of the cache to make room for a younger
     * entry.
     *
     * @param value
     */
    void processRemoveFromLRU(VAL value);
}

/**
 * An LRU cache, based on <code>LinkedHashMap</code>.<br> This cache has a fixed maximum number of elements
 * (<code>m_cacheSize</code>). If the cache is full and another entry is added, the LRU (least recently used) entry is
 * dropped.
 * <p/>
 * This class is thread-safe. All methods of this class are synchronized.<br> Author: Christian d'Heureuse (<a
 * href="http://www.source-code.biz">www.source-code.biz</a>)<br> License: <a href="http://www.gnu.org/licenses/lgpl.html">LGPL</a>.
 * <p/>
 * <p/>
 */
public class LRUHashMap<K, V> implements Map<K, V> {
    private static final float s_hashTableLoadFactor = 0.75f;

    private LinkedHashMap<K, V> m_map;

    private int m_cacheSize;

    private LRURemovalCallback<V> removalCallback;
    private transient Entry<K, V> m_eldestRemovedEntry = null;

    /**
     * Creates a new LRU cache.
     *
     * @param cacheSize the maximum number of entries that will be kept in this cache.
     */
    public LRUHashMap(int cacheSize) {
        this.m_cacheSize = cacheSize;
        int hashTableCapacity = (int) Math
                .ceil(cacheSize / s_hashTableLoadFactor) + 1;
        m_map = new com.hitorro.util.core.map.LRUHashMap.LRULinkedHashMap(hashTableCapacity, s_hashTableLoadFactor, true, false);
        this.removalCallback = null;
    }

    /**
     * Creates a new LRU cache.
     *
     * @param cacheSize the maximum number of entries that will be kept in this cache.
     */
    public LRUHashMap(int cacheSize, LRURemovalCallback<V> removalCallback) {
        this.m_cacheSize = cacheSize;
        int hashTableCapacity = (int) Math
                .ceil(cacheSize / s_hashTableLoadFactor) + 1;
        m_map = new com.hitorro.util.core.map.LRUHashMap.LRULinkedHashMap(hashTableCapacity, s_hashTableLoadFactor, true, removalCallback != null);
        this.removalCallback = removalCallback;
    }

    /**
     * Retrieves an entry from the cache.<br> The retrieved entry becomes the MRU (most recently used) entry.
     *
     * @param key the key whose associated value is to be returned.
     * @return the value associated to this key, or null if no value with this key exists in the cache.
     */
    public synchronized V get(Object key) {
        return m_map.get(key);
    }

    public int size() {
        return m_map.size();
    }

    public boolean isEmpty() {
        return size() == 0;
    }

    public boolean containsKey(Object o) {
        return m_map.containsKey(o);
    }

    public boolean containsValue(Object o) {
        return containsValue(o);
    }


    /**
     * Adds an entry to this cache. If the cache is full, the LRU (least recently used) entry is dropped, and if a
     * removal callback has been registered, it will be called to notify the entry of its removal.
     *
     * @param key   the key with which the specified value is to be associated.
     * @param value a value to be associated with the specified key.
     */
    public V put(K key, V value) {
        /*
         * To avoid deadlock with threads calling get() that might have a cache entry locked, the put method
         * now releases its lock on the cache before calling removalCallBack.processRemoveFromLRU(value), in
         * case processRemoveFromLRU also needs to lock the removed cache entry.
         */
        Entry<K, V> removedEntry;
        synchronized (this) {
            m_map.put(key, value);
            removedEntry = m_eldestRemovedEntry;
            m_eldestRemovedEntry = null;

        }
        if (removedEntry != null) {
            assert removalCallback != null;
            removalCallback.processRemoveFromLRU(removedEntry.getValue());
            return removedEntry.getValue();
        }
        return null;
    }

    /**
     * Clears the cache.
     */
    public synchronized void clear() {
        m_map.clear();
    }

    public Set<K> keySet() {
        return m_map.keySet();
    }

    public Collection<V> values() {
        return m_map.values();
    }

    public Set<Entry<K, V>> entrySet() {
        return m_map.entrySet();
    }

    /**
     * Returns the number of used entries in the cache.
     *
     * @return the number of entries currently in the cache.
     */
    public synchronized int usedEntries() {
        return m_map.size();
    }

    /**
     * Returns the maximum number of entries that the cache can handle
     *
     * @return the maximum size of the cache
     */
    public int maxEntries() {
        return m_cacheSize;
    }

    /**
     * Returns a <code>Collection</code> that contains a copy of all cache entries.
     *
     * @return a <code>Collection</code> with a copy of the cache content.
     */
    public synchronized Collection<Entry<K, V>> getAll() {
        return new ArrayList<Entry<K, V>>(m_map.entrySet());
    }

    /**
     * Returns a removed value for the key
     *
     * @param key the key with which the specified value is to be associated.
     * @return
     */
    public synchronized V remove(Object key) {
        return m_map.remove(key);
    }

    public void putAll(Map<? extends K, ? extends V> map) {
        Set set = map.entrySet();
        Iterator<Entry<K, V>> iter = set.iterator();
        while (iter.hasNext()) {
            Entry<K, V> e = iter.next();
            put(e.getKey(), e.getValue());
        }
    }

    public int getM_cacheSize() {
        return m_cacheSize;
    }

    /**
     * Copy all entries in this cache to another LRU cache.
     *
     * @param destCache the destination cache
     */
    public synchronized void copyAll(com.hitorro.util.core.map.LRUHashMap<K, V> destCache) {
        for (Entry<K, V> entry : m_map.entrySet()) {
            destCache.put(entry.getKey(), entry.getValue());
        }
    }

    class LRULinkedHashMap extends LinkedHashMap<K, V> {
        private static final long serialVersionUID = 1L;
        private final boolean saveEldestRemovedEntry;

        public LRULinkedHashMap(int initialCapacity, float loadFactor, boolean accessOrder,
                                boolean saveEldestRemovedEntry) {
            super(initialCapacity, loadFactor, accessOrder);
            this.saveEldestRemovedEntry = saveEldestRemovedEntry;
        }

        @Override
        protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
            if (size() > com.hitorro.util.core.map.LRUHashMap.this.m_cacheSize) {
                if (saveEldestRemovedEntry) {
                    m_eldestRemovedEntry = eldest;
                }
                return true;
            }
            return false;
        }
    }


}