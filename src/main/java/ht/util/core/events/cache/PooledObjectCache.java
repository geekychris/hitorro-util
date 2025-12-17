package ht.util.core.events.cache;

import ht.util.core.events.WeakReferenceList;
import ht.util.core.iterator.Mapper;
import ht.util.core.iterator.mappers.BaseMapper;

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





