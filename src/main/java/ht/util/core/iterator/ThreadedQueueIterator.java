package ht.util.core.iterator;

import ht.util.core.iterator.sinks.SinkPeerInterface;
import ht.util.core.queue.ThreadedQueue;
import ht.util.core.queue.ThreadedQueueCanceledException;
import ht.util.core.queue.ThreadedQueueTimeoutException;

import java.util.concurrent.TimeUnit;

/**
 * Wrappers a threaded queue in an iterator so that we can turn a farm of processing  back into an apparent single
 * stream of iterable objects.  The objects will not be in the same order that they entered the parallel step.
 */
public class ThreadedQueueIterator<E> extends AbstractIterator<E> implements SinkPeerInterface {
    private E e;
    private ThreadedQueue<E> queue;
    private boolean completed = false;
    private long queueTimeout = 2000;

    public ThreadedQueueIterator(ThreadedQueue<E> queue) {
        this.queue = queue;
        e = getAux();
    }

    @Override
    public void close() throws Exception {
        queue.setQueueCanceled(true);
    }

    @Override
    public boolean hasNext() {
        return e != null;
    }

    @Override
    public E next() {
        E res = e;
        e = getAux();
        return res;
    }

    @Override
    public void remove() {
        // not supported
    }

    @Override
    public boolean completed() {
        return completed;
    }

    private E getAux() {
        while (completed == false) {
            try {
                return queue.poll(queueTimeout, TimeUnit.MILLISECONDS);
            } catch (ThreadedQueueCanceledException e) {

            } catch (ThreadedQueueTimeoutException e) {
                if (queue.getQueueComplete()) {
                    completed = true;
                    return null;
                }
            }
        }
        return null;
    }
}
