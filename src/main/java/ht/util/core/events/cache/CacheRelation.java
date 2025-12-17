package ht.util.core.events.cache;

/**
 *
 */
public class CacheRelation {
    private Cache rootCache;
    private Cache derivedCache;

    /**
     * @param rootCache    cache that the derived cache depends on
     * @param derivedCache
     */
    public CacheRelation(Cache rootCache, Cache derivedCache) {
        this.rootCache = rootCache;
        this.derivedCache = derivedCache;
        rootCache.addCacheRelation(this);
        CacheRegistry.getMe().registerRelation(this);
    }

    /**
     * If the root cache is flushed then the derived cache needs flushing too?
     */
    public void flushAll() {
        derivedCache.flushCacheBit();
    }

    public void deleteFromCache(Object o) {
        derivedCache.deleteFromCache(o);
    }
}
