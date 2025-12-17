package ht.util.core.iterator.queue;

import ht.util.core.queue.EmptyQueueCallback;

import java.util.Collection;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

public class FilteringDequeue<E> extends AbstractDequeue<E> {
    private AbstractDequeue<E> queue;
    private Predicate<E> predicate;

    public FilteringDequeue(AbstractDequeue<E> queue, Predicate<E> predicate) {
        this.queue = queue;
        this.predicate = predicate;
    }

    public void setQueueEmptyCallback(EmptyQueueCallback q) {
        queue.setQueueEmptyCallback(q);
    }

    @Override
    public int size() {
        return queue.size();
    }

    @Override
    public boolean isCompleted() {
        return queue.isCompleted();
    }

    @Override
    public boolean getQueueCanceled() {
        return queue.getQueueCanceled();
    }

    @Override
    public E take() throws InterruptedException {
        while (true) {
            E ret = null;
            ret = queue.take();
            if (predicate.test(ret)) {
                return ret;
            }
        }
    }

    @Override
    public E poll(final long timeout, final TimeUnit unit) throws InterruptedException {
        while (true) {
            E ret = null;
            ret = queue.poll(timeout, unit);
            if (ret == null) {
                // timed out
                return null;
            }
            if (predicate.test(ret)) {
                return ret;
            }
        }
    }

    @Override
    public int remainingCapacity() {
        return queue.remainingCapacity();
    }

    @Override
    public int drainTo(final Collection<? super E> c) {
        int count = 0;
        while (queue.remainingCapacity() > 0) {
            count = getCount((Collection<? super E>) c, count);

        }
        return count;
    }

    @Override
    public int drainTo(final Collection<? super E> c, final int maxElements) {
        int count = 0;
        while (queue.remainingCapacity() > 0 && count < maxElements) {
            count = getCount((Collection<? super E>) c, count);
        }
        return count;
    }

    private int getCount(final Collection<? super E> c, int count) {
        try {
            E e = queue.take();
            if (predicate.test(e)) {
                c.add(e);
                count++;
            }
        } catch (InterruptedException e1) {

        }
        return count;
    }
}
