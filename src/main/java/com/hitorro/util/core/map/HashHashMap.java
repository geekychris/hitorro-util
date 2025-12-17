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

import com.hitorro.util.core.GenericKeyValue;
import com.hitorro.util.core.iterator.AbstractIterator;
import com.hitorro.util.core.iterator.CollectionIterator;

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

