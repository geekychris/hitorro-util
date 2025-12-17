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

import com.hitorro.util.core.iterator.queue.AbstractDequeue;

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
