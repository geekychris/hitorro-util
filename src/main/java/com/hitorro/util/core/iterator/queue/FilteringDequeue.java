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

import com.hitorro.util.core.queue.EmptyQueueCallback;

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
