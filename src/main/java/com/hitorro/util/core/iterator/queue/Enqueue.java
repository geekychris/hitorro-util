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
package com.hitorro.util.core.iterator.queue;

import com.fasterxml.jackson.databind.JsonNode;
import com.hitorro.util.core.queue.ThreadedQueue;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class Enqueue<E> extends AbstractEnqueue<E> {
    private ThreadedQueue<E> queue;
    private Dequeue dequeue = null;

    public Enqueue(ThreadedQueue<E> queue) {
        this.queue = queue;
    }

    public AbstractDequeue<E> dequeue() {
        if (dequeue == null) {
            dequeue = new Dequeue<E>(queue);
            dequeue.setEnqueue(this);
        }
        return dequeue;
    }

    public void clear() {
        queue.clear();
    }

    @Override
    public boolean init(final JsonNode node) {
        return true;
    }

    @Override
    public boolean add(final E e) {
        return notify(queue.add(e));
    }

    @Override
    public void put(final E e) throws InterruptedException {
        queue.put(e);
        notify(true);
    }

    @Override
    public void close() throws IOException {
        queueComplete = true;
    }

    @Override
    public boolean offer(final E e) {
        return notify(queue.offer(e));
    }


    public E remove(final Object o) {
        return notify(queue.remove());
    }

    public int size() {
        return queue.size();
    }

    public int remainingCapacity() {
        return queue.remainingCapacity();
    }

    @Override
    public boolean offer(final E e, final long timeout, final TimeUnit unit) throws InterruptedException {
        return notify(queue.offer(e, timeout, unit));
    }

    protected boolean notify(boolean flag) {
        if (notifier != null) {
            synchronized (notifier) {
                notifier.notify();
            }
        }
        return flag;
    }

    protected E notify(E o) {
        if (notifier != null) {
            synchronized (notifier) {
                notifier.notify();
                ;
            }
        }
        return o;
    }

    public boolean getQueueCanceled() {
        return queueCanceled;
    }

    public void setQueueCanceled(boolean flag) {
        this.queueCanceled = flag;
    }

    public void setQueueComplete() {
        queueComplete = true;
    }

    public boolean getQueueComplete() {
        return queueComplete;
    }

    public Object getNotifier() {
        return notifier;
    }

    public void setNotifier(Object notifier) {
        this.notifier = notifier;
    }
}
