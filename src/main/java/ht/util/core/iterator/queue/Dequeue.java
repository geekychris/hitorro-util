package ht.util.core.iterator.queue;

import ht.util.core.queue.ThreadedQueue;
import ht.util.core.queue.ThreadedQueueCanceledException;

import java.util.Collection;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public class Dequeue<E> extends AbstractDequeue<E> {
    protected ThreadedQueue<E> queue;

    public Dequeue(ThreadedQueue<E> queue) {
        this.queue = queue;
    }

    @Override
    public int size() {
        return queue.size();
    }

    @Override
    public boolean isCompleted() {
        return queue.getQueueComplete();
    }

    @Override
    public boolean getQueueCanceled() {
        return queue.getQueueCanceled();
    }

    @Override
    public E take() throws InterruptedException {
        if (queue.getQueueCanceled()) {
            throw new ThreadedQueueCanceledException();
        }
        return queue.take();
    }

    @Override
    public E poll(final long timeout, final TimeUnit unit) throws InterruptedException {
        if (queue.getQueueCanceled()) {
            throw new ThreadedQueueCanceledException();
        }
        if (queue.isEmpty() && emptyQCallback != null) {
            emptyQCallback.empty();
        }
        return queue.poll(timeout, unit);
    }

    @Override
    public int remainingCapacity() {
        if (queue.getQueueCanceled()) {
            throw new ThreadedQueueCanceledException();
        }
        return queue.remainingCapacity();
    }

    @Override
    public int drainTo(final Collection<? super E> c) {
        if (queue.getQueueCanceled()) {
            throw new ThreadedQueueCanceledException();
        }
        int count = queue.drainTo(c);
        if (queue.isEmpty() && emptyQCallback != null) {
            emptyQCallback.empty();
        }
        return count;
    }

    @Override
    public int drainTo(final Collection<? super E> c, final int maxElements) {
        if (queue.getQueueCanceled()) {
            throw new ThreadedQueueCanceledException();
        }
        int count = queue.drainTo(c, maxElements);
        if (queue.isEmpty() && emptyQCallback != null) {
            emptyQCallback.empty();
        }
        return count;
    }
}
