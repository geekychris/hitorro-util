package ht.util.core.queue;

import ht.util.core.iterator.queue.Enqueue;

import java.util.concurrent.BlockingQueue;

public abstract class OnlyOnceEnqueue<E> extends Enqueue<E> {
    private Object mutex = new Object();
    public OnlyOnceEnqueue(ThreadedQueue<E> queue) {
        super(queue);
    }

    @Override
    public boolean add(final E e) {
        synchronized (mutex) {
            if (exists(e)) {
                return true;
            }
            remember(e);
            return super.add(e);
        }
    }

    @Override
    public void put(final E e) throws InterruptedException {
        synchronized (mutex) {
            if (exists(e)) {
                return;
            }
            remember(e);
            super.put(e);
        }
    }

    protected abstract  boolean exists (final E e);
    protected abstract void remember (final E e);
}
