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

import com.hitorro.util.core.queue.ThreadedQueue;
import com.hitorro.util.core.queue.ThreadedQueueCanceledException;

import java.util.Collection;
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
