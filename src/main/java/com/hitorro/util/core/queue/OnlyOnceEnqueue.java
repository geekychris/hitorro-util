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
package com.hitorro.util.core.queue;

import com.hitorro.util.core.iterator.queue.Enqueue;

public abstract class OnlyOnceEnqueue<E> extends Enqueue<E> {
    private Object mutex = new Object();
    public OnlyOnceEnqueue(ThreadedQueue<E> queue) {
        super(queue);
    }

    @Override
    public boolean add(final E e) {
        synchronized (mutex) {
            if (exists(e)) {
                return true;
            }
            remember(e);
            return super.add(e);
        }
    }

    @Override
    public void put(final E e) throws InterruptedException {
        synchronized (mutex) {
            if (exists(e)) {
                return;
            }
            remember(e);
            super.put(e);
        }
    }

    protected abstract  boolean exists (final E e);
    protected abstract void remember (final E e);
}
