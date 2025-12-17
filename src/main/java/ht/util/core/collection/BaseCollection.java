package ht.util.core.collection;

import com.fasterxml.jackson.databind.JsonNode;
import ht.util.core.iterator.AbstractIterator;
import ht.util.core.iterator.CollectionIterator;
import ht.util.core.iterator.sinks.Sink;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Function;
import java.util.function.Predicate;

public class BaseCollection<K, E> implements Sink<E> {
    private ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    private List<E> list = new ArrayList();
    private BaseCollectionChangeNotifier<E> notifier = new NullNotifier();
    private Comparator<E> comparitor = null;
    private Map<K, E> keyMap = new HashMap();
    private Function<E, K> keyGenerator;

    public void setNotifier(BaseCollectionChangeNotifier<E> n) {
        this.notifier = n;
    }

    public void setComparable(Comparator<E> pred) {
        this.comparitor = pred;
    }

    public void setKeyGenerator(Function<E, K> keyGenerator) {
        this.keyGenerator = keyGenerator;
    }


    public AbstractIterator<E> getIterator(Predicate<E> pred, boolean remove) {
        List<E> list = new ArrayList();
        findAll(pred, list, remove);
        return new CollectionIterator<>(list);
    }

    @Override
    public boolean init(final JsonNode node) {
        return true;
    }

    @Override
    public boolean start() {
        return true;
    }

    public boolean add(E e) {
        take(true);
        try {
            list.add(e);
            notifier.added(e);
            if (keyGenerator != null) {
                keyMap.put(keyGenerator.apply(e), e);
            }
        } finally {
            release(true);
        }
        return true;
    }

    @Override
    public boolean stop() throws IOException {
        return false;
    }

    @Override
    public void close() throws IOException {

    }

    public int visit(Predicate<E> pred, BaseCollectionVisitor<E> visitor, boolean modifies) {
        int count = 0;
        take(modifies);
        try {
            int i = list.size() - 1;
            for (; i >= 0; i--) {
                E e = list.get(i);
                if (pred.test(e)) {
                    count++;
                    visitor.visit(e);
                }
            }
        } finally {
            release(modifies);
        }
        return count;
    }

    /**
     * Replace an item in the collection.  There must be an object comparitor setup for this to function correctly.
     *
     * @param e
     * @return
     */
    public E replace(E e) {
        take(true);

        try {
            // attempt to use a hash lookup first.
            if (keyGenerator != null) {
                K k = keyGenerator.apply(e);
                E old = keyMap.replace(k, e);
                if (old != null) {
                    notifier.removed(old);
                }
                notifier.added(e);
                return old;
            }

            for (int i = 0; i < list.size(); i++) {
                E old = list.get(i);
                if (comparitor != null) {
                    if (comparitor.compare(old, e) == 0) {
                        // same, so lets replace
                        E ret = list.set(i, e);
                        notifier.removed(ret);
                        notifier.added(e);
                        return ret;
                    }
                }
            }
        } finally {
            release(true);
        }
        return null;
    }

    public int findAll(Predicate<E> pred, Collection<E> coll, boolean remove) {
        int count = 0;
        take(remove);
        try {
            int i = list.size() - 1;
            for (; i >= 0; i--) {
                E e = list.get(i);
                if (pred.test(e)) {
                    count++;
                    if (remove) {
                        list.remove(i);
                        notifier.removed(e);
                        removeFromMap(e);
                    }
                    pred.test(e);
                    coll.add(e);
                }

            }
            return count;
        } finally {
            release(remove);
        }
    }

    private void removeFromMap(final E e) {
        if (keyGenerator != null) {
            keyMap.remove(keyGenerator.apply(e));
        }
    }


    public E findFirst(Predicate<E> pred, boolean remove) {
        take(remove);
        try {
            int i = list.size() - 1;
            for (; i >= 0; i--) {
                E e = list.get(i);
                if (pred.test(e)) {
                    if (remove) {
                        list.remove(i);
                        notifier.removed(e);
                        removeFromMap(e);
                    }
                    return e;
                }

            }
            return null;
        } finally {
            release(remove);
        }

    }


    private void take(boolean write) {
        if (write) {
            readWriteLock.writeLock().lock();
        } else {
            readWriteLock.readLock().lock();
        }
    }

    private void release(boolean write) {
        if (write) {
            readWriteLock.writeLock().unlock();
        } else {
            readWriteLock.readLock().unlock();
        }
    }
}
