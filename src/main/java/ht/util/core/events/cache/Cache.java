package ht.util.core.events.cache;

import ht.util.core.events.EventListener;
import ht.util.core.events.LocalEventHub;

import java.util.ArrayList;
import java.util.List;

/**
 * Copyright (c) 2003 - present HiTorro All rights reserved. User: chris Date: Oct 17, 2006 Time: 6:39:40 PM
 */
public abstract class Cache<K, V> implements EventListener {
    // Indicate the "Desire" to remove all items from the cache
    public static final String FlushCache = "FlushCache";
    // Indicate the "desire" to remove n items from the cache, provided by key name
    public static final String DeleteItem = "DeleteItem";
    // Indicate the desire to update an item in the cache by key name
    public static final String UpdateItem = "UpdateItem";
    // Indicate the desire to put items to the cache by key name
    public static final String AddItem = "AddItem";
    protected boolean m_runAsync = false;
    protected String eventName;
    protected List<CacheRelation> relations = new ArrayList<>();
    private long m_refreshInterval = 0;
    private long m_lastRefresh = 0;
    private boolean m_considerCacheFlush = false;

    public Cache() {

    }

    /**
     * Constructor that defines a period in millis that we will hold this cache as fresh.  Beyond such point it is
     * considered dirty.
     *
     * @param refreshInterval
     */
    public Cache(long refreshInterval, String eventName) {
        m_refreshInterval = refreshInterval;
        this.eventName = eventName;
        // all things get registered with the event bus
        LocalEventHub.get().addEventListener(this, eventName);
        CacheRegistry.getMe().register(this);
    }

    public abstract String getDescription();

    public void addCacheRelation(CacheRelation c) {
        relations.add(c);
    }

    protected void flushAllRelated() {
        if (relations.size() == 0) {
            return;
        }
        for (CacheRelation cr : relations) {
            cr.flushAll();
        }
    }

    protected void flushAllRelatedKey(Object key) {
        if (relations.size() == 0) {
            return;
        }
        for (CacheRelation cr : relations) {
            cr.deleteFromCache(key);
        }
    }

    public boolean runAsync() {
        return m_runAsync;
    }

    /**
     * Notify this cache.
     *
     * @param eventName name of the topic (such as config params)
     * @param subEvent  subtopic for event (such as flush, delete
     * @param args      option listFiles of arguments specific to eventName, subEvent compContext
     */
    public boolean event(String eventName, String subEvent, Object args) {
        {
            if (subEvent.equals(FlushCache)) {
                flushCacheBit();
                flushAllRelated();
                return true;
            }
            if (subEvent.equals(DeleteItem)) {
                deleteFromCache(args);
                flushAllRelatedKey(args);
                return true;
            }
            if (subEvent.equals(UpdateItem)) {
                updateCache(args);
                flushAllRelatedKey(args);
                return true;
            }
            if (subEvent.equals(AddItem)) {
                addToCache(args);
                return true;
            }
        }
        // dont know the operation
        return false;
    }

    /**
     * This operation DOES NOT GUARANTEE that the keys will be added to the cache.  They are simply messaged to the
     * cache that they could be cached.
     *
     * @param keys of items that could be added to the cache
     */
    protected abstract void addToCache(Object keys);

    /**
     * This operation DOES NOT GUARANTEE that the keys will be updated to the cache.  They are simply messaged to the
     * cache that they could be cached.
     *
     * @param keys of items that could be added to the cache
     */
    protected abstract void updateCache(Object keys);

    /**
     * This operation DOES NOT GUARANTEE that the keys will be deleted to the cache.  They are simply messaged to the
     * cache that they could be cached.
     *
     * @param keys of items that could be deleted to the cache
     */
    protected abstract void deleteFromCache(Object keys);

    /**
     * internal mechanism to indicate that we should remove our cached content the next time we have chance.
     */
    public void flushCacheBit() {
        m_considerCacheFlush = true;
    }


    /**
     * @return true if we are either marked for flush or time has elapsed based upon refresh interval
     */
    protected boolean considerCacheFlush() {
        if (m_considerCacheFlush) {
            return true;
        }
        if (m_refreshInterval > 0) {
            // we are keeping a refresh interval
            long curr = System.currentTimeMillis();
            return (m_lastRefresh + m_refreshInterval) < curr;
        }
        return false;
    }


    /**
     * Reset not only the consider reset cache boolean but the last reset point.
     */
    protected void resetConsiderClearingCache() {
        m_considerCacheFlush = false;
        m_lastRefresh = System.currentTimeMillis();
    }

    public String eventName() {
        return eventName;
    }
}
