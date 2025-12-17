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

import com.hitorro.util.core.iterator.AbstractIterator;
import com.hitorro.util.core.iterator.DequeueIterator;
import com.hitorro.util.core.queue.EmptyQueueCallback;

import java.util.Collection;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Predicate;

public abstract class AbstractDequeue<E> {
    private AbstractEnqueue<E> enqueue;
    protected EmptyQueueCallback emptyQCallback;

    public <F> AbstractDequeue<F> map(Function<E, F> function) {
        return new MappingDequeue(this, function);
    }

    public AbstractDequeue<E> filter(Predicate<E> predicate) {
        return new FilteringDequeue(this, predicate);
    }

    public AbstractIterator<E> iterator(long maxTime, TimeUnit unit) {
        return new DequeueIterator(this, maxTime, unit, false);
    }

    public void setQueueEmptyCallback(EmptyQueueCallback q) {
        emptyQCallback = q;
    }

    public AbstractIterator<E> iterator() {
        return new DequeueIterator(this, 1000, TimeUnit.MILLISECONDS, true);
    }

    public abstract int size();

    public abstract boolean isCompleted();

    public abstract boolean getQueueCanceled();

    public AbstractEnqueue getEnqueue() {
        return enqueue;
    }

    void setEnqueue(AbstractEnqueue<E> enqueue) {
        this.enqueue = enqueue;
    }

    /**
     * Retrieves and removes the head of this queue, waiting if necessary
     * until an element becomes available.
     *
     * @return the head of this queue
     * @throws InterruptedException if interrupted while waiting
     */
    public abstract E take() throws InterruptedException;

    /**
     * Retrieves and removes the head of this queue, waiting up to the
     * specified wait time if necessary for an element to become available.
     *
     * @param timeout how long to wait before giving up, in units of
     *                {@code unit}
     * @param unit    a {@code TimeUnit} determining how to interpret the
     *                {@code timeout} parameter
     * @return the head of this queue, or {@code null} if the
     * specified waiting time elapses before an element is available
     * @throws InterruptedException if interrupted while waiting
     */
    public abstract E poll(long timeout, TimeUnit unit)
            throws InterruptedException;

    /**
     * Returns the number of additional elements that this queue can ideally
     * (in the absence of memory or resource constraints) accept without
     * blocking, or {@code Integer.MAX_VALUE} if there is no intrinsic
     * limit.
     *
     * <p>Note that you <em>cannot</em> always tell if an attempt to insert
     * an element will succeed by inspecting {@code remainingCapacity}
     * because it may be the case that another thread is about to
     * insert or remove an element.
     *
     * @return the remaining capacity
     */
    public abstract int remainingCapacity();

    /**
     * Removes all available elements from this queue and adds them
     * to the given collection.  This operation may be more
     * efficient than repeatedly polling this queue.  A failure
     * encountered while attempting to put elements to
     * collection {@code c} may result in elements being in neither,
     * either or both collections when the associated exception is
     * thrown.  Attempts to drain a queue to itself result in
     * {@code IllegalArgumentException}. Further, the behavior of
     * this operation is undefined if the specified collection is
     * modified while the operation is in progress.
     *
     * @param c the collection to transfer elements into
     * @return the number of elements transferred
     * @throws UnsupportedOperationException if addition of elements
     *                                       is not supported by the specified collection
     * @throws ClassCastException            if the class of an element of this queue
     *                                       prevents it from being added to the specified collection
     * @throws NullPointerException          if the specified collection is null
     * @throws IllegalArgumentException      if the specified collection is this
     *                                       queue, or some property of an element of this queue prevents
     *                                       it from being added to the specified collection
     */
    public abstract int drainTo(Collection<? super E> c);

    /**
     * Removes at most the given number of available elements from
     * this queue and adds them to the given collection.  A failure
     * encountered while attempting to put elements to
     * collection {@code c} may result in elements being in neither,
     * either or both collections when the associated exception is
     * thrown.  Attempts to drain a queue to itself result in
     * {@code IllegalArgumentException}. Further, the behavior of
     * this operation is undefined if the specified collection is
     * modified while the operation is in progress.
     *
     * @param c           the collection to transfer elements into
     * @param maxElements the maximum number of elements to transfer
     * @return the number of elements transferred
     * @throws UnsupportedOperationException if addition of elements
     *                                       is not supported by the specified collection
     * @throws ClassCastException            if the class of an element of this queue
     *                                       prevents it from being added to the specified collection
     * @throws NullPointerException          if the specified collection is null
     * @throws IllegalArgumentException      if the specified collection is this
     *                                       queue, or some property of an element of this queue prevents
     *                                       it from being added to the specified collection
     */
    public abstract int drainTo(Collection<? super E> c, int maxElements);
}
