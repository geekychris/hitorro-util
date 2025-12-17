package ht.util.core.events.cache;

public interface PooledObjectIntf<K> extends AutoCloseable {
    int getGenerationId();

    void setGenerationId(int id);

    void passivate();

    void activate();

    /**
     * If the object supports being re-initialized from the key then it can return true.  This allows that objects being
     * returned after a pool has been flushed are able to return the object to the pool post a request to init it.
     *
     * @param key
     * @return
     */
    boolean reInit(K key);

    void setPoolContainer(PoolContainer pc);
}