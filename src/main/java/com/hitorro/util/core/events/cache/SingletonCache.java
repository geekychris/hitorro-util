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
import org.apache.poi.ss.formula.functions.T;

import java.util.List;

/**
 */
public class SingletonCache<E> extends Cache<Object, E> {
    private Object m_lock = new Object();
    private E m_value;
    private boolean m_demandBasedCacheing = true;

    private Mapper<Object, E> mapper;
    private T objectDefaultValue;

    public SingletonCache(String eventName, Mapper<Object, E> mapper) {
        this(true, false, eventName, mapper, null);
    }

    /**
     * Constructor
     *
     * @param demandBasedCacheing if set to false, then all put, updates' get recognized as operations that need to be
     *                            performed. If set to true, then deletes delete and updates delete immediately.
     * @param asyncFetch          If set to true, if an event notifies us of a cache flush, we will assume that we must
     *                            fetch a new copy in the background. the event listener hub uses this flag to indicate
     *                            that the event will be called with a seperate thread (thus not hanging the hub) so
     *                            that a long lasting process can be done in its own private thread of execution.
     */
    public SingletonCache(boolean demandBasedCacheing,
                          boolean asyncFetch,
                          String eventName, Mapper<Object, E> mapper,
                          T objectDefaultValue) {
        super(0, eventName);
        m_demandBasedCacheing = demandBasedCacheing;
        this.m_runAsync = asyncFetch;
        this.mapper = mapper;
        this.objectDefaultValue = objectDefaultValue;
    }


    public String getDescription() {
        return Fmt.S("SingletonCache for %s using apply %s", eventName(), mapper.getClass().getName());
    }

    @Override
    public void flushCacheBit() {
        if (this.runAsync()) {
            // we are in async mode, which means that we will
            // mark the item flushed, instead we will update the
            // item ourselfs.
            // thus when the item is stale we will continue using this
            // till we get a replacement item from this call.
            synchronized (this) {
                getAndCache();
            }
        } else {
            super.flushCacheBit();
        }
        flushAllRelated();
    }

    /**
     * Get a single value carried in this singleton cache.
     *
     * @return single value of the singleton
     */
    public E get() {
        synchronized (m_lock) {
            if (considerCacheFlush()) {
                if (meetsFlushCriteria()) {
                    flushCache();
                }
                resetConsiderClearingCache();
            }
            if (m_value != null) {
                return m_value;
            }
            return getAndCache();
        }
    }

    /**
     * Private helper method to actualy compute the value assuming it has to be computed/ re computed.
     *
     * @return
     */
    protected E getAndCache() {
        E result = getAux();
        if (result != null) {
            // store result
            m_value = result;
            return result;
        }
        return null;
    }

    /**
     * This operation DOES NOT GUARANTEE that the keys will be added to the cache.  They are simply messaged to the
     * cache that they could be cached.
     *
     * @param l of items that could be added to the cache
     */
    @Override
    protected void addToCache(Object l) {
        if (!m_demandBasedCacheing) {
            if (l instanceof List) {
                List keys = (List) l;
                synchronized (m_lock) {
                    // only do this if we are not demand based.
                    m_value = getAux();
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
    @Override
    protected void updateCache(Object l) {
        synchronized (m_lock) {
            if (l instanceof List) {
                List keys = (List) l;
                if (!m_demandBasedCacheing) {

                    // only do this if we are not demand based.
                    this.m_value = getAux();
                } else {
                    // just nuke it
                    m_value = null;
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
    @Override
    protected void deleteFromCache(Object l) {
        synchronized (m_lock) {
            if (l instanceof List) {
                List keys = (List) l;
                m_value = null;
            }
        }
    }

    /**
     * Must implement this guy to retrieve the object requested from the cache.
     *
     * @return
     */
    protected E getAux() {
        return mapper.apply(objectDefaultValue);
    }

    /**
     * Custom test
     *
     * @return true if then flush, else ignore flush request
     */
    protected boolean meetsFlushCriteria() {
        return true;
    }

    protected void flushCache() {
        m_value = null;
    }
}
