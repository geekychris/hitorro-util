package ht.util.core.map;

import ht.util.core.GenericKeyValue;
import ht.util.core.iterator.AbstractIterator;
import ht.util.core.iterator.CollectionIterator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Two layer hash
 */
public class HashHashMap<L1TYPE extends Object, L2TYPE extends Object, PAYLOAD extends Object> {
    private Map<L1TYPE, Map<L2TYPE, PAYLOAD>> root = getNewMap();
    private ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    private HashHashMapFactory<L1TYPE, L2TYPE, PAYLOAD> factory;

    public HashHashMap(HashHashMapFactory<L1TYPE, L2TYPE, PAYLOAD> factory) {
        this.factory = factory;
    }

    private HashHashMap() {
    }

    public AbstractIterator<GenericKeyValue<L1TYPE, L2TYPE>> getNodesIterator() {
        return new CollectionIterator(getNodesList());
    }

    public List<GenericKeyValue<L1TYPE, L2TYPE>> getNodesList() {
        try {
            readWriteLock.readLock().lock();
            List<GenericKeyValue<L1TYPE, L2TYPE>> list = new ArrayList();
            for (L1TYPE m : root.keySet()) {
                Map<L2TYPE, PAYLOAD> map = root.get(m);
                for (L2TYPE n : map.keySet()) {
                    list.add(new GenericKeyValue(m, n));
                }
            }
            return list;
        } finally {
            readWriteLock.readLock().unlock();
        }
    }

    public PAYLOAD get(L1TYPE m, L2TYPE n) {
        return get(m, n, false);
    }

    public PAYLOAD get(L1TYPE m, L2TYPE n, boolean createIfMissing) {
        PAYLOAD p = getAux(readWriteLock.readLock(), m, n, false);
        if (p == null && createIfMissing) {
            return getAux(readWriteLock.writeLock(), m, n, createIfMissing);
        }
        return p;
    }

    private PAYLOAD getAux(Lock lock, L1TYPE m, L2TYPE n, boolean createIfMissing) {
        lock.lock();
        try {
            Map<L2TYPE, PAYLOAD> child = root.get(m);
            PAYLOAD p = null;
            if (child != null) {
                p = child.get(n);
                if (p != null) {
                    return p;
                }
            }
            if (createIfMissing && factory != null) {
                p = factory.create(m, n);
                if (p != null) {
                    put(m, n, p);
                }
            }
            return p;
        } finally {
            lock.unlock();
        }
    }

    public PAYLOAD put(L1TYPE m, L2TYPE n, PAYLOAD p) {
        readWriteLock.writeLock().lock();
        try {
            Map<L2TYPE, PAYLOAD> child = root.get(m);
            if (child == null) {
                child = getNewMap();
                root.put(m, child);
            }
            return child.put(n, p);
        } finally {
            readWriteLock.writeLock().unlock();
        }
    }

    public PAYLOAD remove(L1TYPE m, L2TYPE n) {
        readWriteLock.writeLock().lock();
        try {
            Map<L2TYPE, PAYLOAD> child = root.get(m);
            PAYLOAD p = null;
            if (child != null) {
                p = child.remove(n);
                if (child.size() == 0) {
                    root.remove(m);
                }
            }
            return p;
        } finally {
            readWriteLock.writeLock().unlock();
        }
    }

    public Map getNewMap() {
        return new TreeMap();
    }
}

