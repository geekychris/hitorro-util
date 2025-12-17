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
import java.util.function.Function;

public class MappingDequeue<D, E> extends AbstractDequeue<E> {
    private AbstractDequeue<D> queue;
    private Function<D, E> mappingFunction;

    public MappingDequeue(AbstractDequeue<D> queue, Function<D, E> mappingFunction) {
        this.queue = queue;
        this.mappingFunction = mappingFunction;
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
        return mappingFunction.apply(queue.take());
    }

    @Override
    public E poll(final long timeout, final TimeUnit unit) throws InterruptedException {
        D d = queue.poll(timeout, unit);
        if (d == null) {
            return null;
        }
        return mappingFunction.apply(d);
    }

    @Override
    public int remainingCapacity() {
        return queue.remainingCapacity();
    }

    @Override
    public int drainTo(final Collection<? super E> c) {
        int count = 0;
        while (queue.remainingCapacity() > 0) {
            try {
                c.add(mappingFunction.apply(queue.take()));
                count++;
            } catch (InterruptedException e) {

            }
        }
        return count;
    }

    @Override
    public int drainTo(final Collection<? super E> c, final int maxElements) {
        int count = 0;
        while (queue.remainingCapacity() > 0 && count < maxElements) {
            try {
                c.add(mappingFunction.apply(queue.take()));
                count++;
            } catch (InterruptedException e) {

            }
        }
        return count;
    }
}
