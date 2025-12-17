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
package com.hitorro.util.core.iterator.sinks;

import com.hitorro.util.core.Log;
import com.hitorro.util.core.queue.ThreadedQueue;
import com.hitorro.util.core.queue.ThreadedQueueCanceledException;
import com.hitorro.util.core.queue.ThreadedQueueTimeoutException;
import com.hitorro.util.core.thread.EnhancedThreadGroup;
import com.hitorro.util.core.thread.HTThread;
import com.hitorro.util.io.StoreException;

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
