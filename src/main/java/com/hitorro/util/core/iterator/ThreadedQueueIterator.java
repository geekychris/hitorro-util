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
package com.hitorro.util.core.iterator;

import com.hitorro.util.core.iterator.sinks.SinkPeerInterface;
import com.hitorro.util.core.queue.ThreadedQueue;
import com.hitorro.util.core.queue.ThreadedQueueCanceledException;
import com.hitorro.util.core.queue.ThreadedQueueTimeoutException;

import java.util.concurrent.TimeUnit;

/**
 * Wrappers a threaded queue in an iterator so that we can turn a farm of processing  back into an apparent single
 * stream of iterable objects.  The objects will not be in the same order that they entered the parallel step.
 */
public class ThreadedQueueIterator<E> extends AbstractIterator<E> implements SinkPeerInterface {
    private E e;
    private ThreadedQueue<E> queue;
    private volatile boolean completed = false;
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
