package ht.util.core.iterator;

import ht.util.core.iterator.queue.AbstractDequeue;

import java.util.concurrent.TimeUnit;

/**
 * Iterate
 *
 * @param <E>
 */
public class DequeueIterator<E> extends AbstractIterator<E> {
    private AbstractDequeue<E> dequeue;
    private long maxTime;
    private TimeUnit unit;
    private boolean retry;

    public DequeueIterator(AbstractDequeue<E> dequeue, long maxTime, TimeUnit unit, boolean retry) {
        this.dequeue = dequeue;
        this.maxTime = maxTime;
        this.unit = unit;
        this.retry = retry;
    }

    @Override
    public boolean hasNext() {
        return !(dequeue.isCompleted() | dequeue.getQueueCanceled());
    }

    @Override
    public E next() {
        do {
            try {
                E elem = dequeue.poll(maxTime, unit);
                if (elem != null) {
                    return elem;
                }
            } catch (InterruptedException e) {
            }
        }
        while (!(dequeue.isCompleted() | dequeue.getQueueCanceled()) && retry);
        return null;
    }
}
