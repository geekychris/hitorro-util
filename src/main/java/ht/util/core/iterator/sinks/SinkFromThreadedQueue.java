package ht.util.core.iterator.sinks;

import ht.util.core.Log;
import ht.util.core.queue.ThreadedQueue;
import ht.util.core.queue.ThreadedQueueCanceledException;
import ht.util.core.queue.ThreadedQueueTimeoutException;
import ht.util.core.thread.EnhancedThreadGroup;
import ht.util.core.thread.HTThread;
import ht.util.io.StoreException;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Receiver of objects from a threaded queue.
 * <p/>
 * Sinks from the queue to a put.  Creates its own thread, but if that thread dies it will not restart it
 */
public class SinkFromThreadedQueue<E> implements Runnable, SinkPeerInterface {
    private Sink<E> sink;
    private ThreadedQueue<E> queue;
    private long queueTimeout = 2000;
    private EnhancedThreadGroup tg;
    private HTThread thread;
    private boolean completed = false;

    /**
     * Construct a put for a threaded queue, optional thread
     *
     * @param queue
     * @param sink
     * @param name
     * @param startAThread
     */
    public SinkFromThreadedQueue(ThreadedQueue<E> queue, Sink<E> sink, String name, boolean startAThread) {
        if (startAThread) {
            tg = new EnhancedThreadGroup(name);
            thread = new HTThread(tg, this, name);
        }
        this.queue = queue;
        this.sink = sink;
        try {
            sink.start();
        } catch (IOException e) {
            Log.queue.error("Unable to take to put %s %e", e, e);
        }
        if (startAThread) {
            thread.start();
        }
    }

    public boolean completed() {
        return completed;
    }

    @Override
    public void run() {
        while (true) {
            try {
                E e = queue.poll(queueTimeout, TimeUnit.MILLISECONDS);
                sink.add(e);
            } catch (ThreadedQueueCanceledException e) {
                Log.queue.error("Unable to take to put %s %e", e, e);
            } catch (ThreadedQueueTimeoutException e) {
                if (queue.getQueueComplete()) {
                    // we have a complete queue, we should exit but
                    try {
                        sink.stop();
                        // done, thread should stop
                        completed = true;
                        return;
                    } catch (IOException e1) {
                        Log.queue.error("Unable to put %s %e", e1, e1);
                    }
                    break;
                }
                Log.queue.error("Unable to take to put %s %e", e, e);
            } catch (StoreException e) {
                // put error
                Log.queue.error("Unable to take to put %s %e", e, e);
            } catch (IOException e) {
                // put error
                Log.queue.error("Unable to take to put %s %e", e, e);
            }
        }
    }
}
