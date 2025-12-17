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

import com.hitorro.util.core.iterator.sinks.Sink;
import com.hitorro.util.core.queue.ThreadedQueue;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Predicate;

public abstract class AbstractEnqueue<E> implements Sink<E> {
    protected Object notifier;
    protected boolean queueCanceled = false;
    protected boolean queueComplete = false;
    protected String queueName;

    public static AbstractEnqueue arrayBlocking(int size) {
        // not Array Blocking queue!!!
        return new Enqueue(new ThreadedQueue(size));
    }

    public static <E> AbstractEnqueue<E> tee(AbstractEnqueue<E>... queues) {
        Queue<AbstractEnqueue<E>> l = new LinkedList<AbstractEnqueue<E>>();
        for (AbstractEnqueue<E> e : queues) {
            l.add(e);
        }

        while (l.size() > 1) {
            l.add(new TeeEnqueue<E>(l.poll(), l.poll()));
        }
        return ((LinkedList<AbstractEnqueue<E>>) l).get(0);
    }

    public <F> AbstractEnqueue<F> map(Function<F, E> mappingFunction) {
        return new MappingEnqueue(this, mappingFunction);
    }

    public boolean start() throws IOException {
        return true;
    }

    public boolean stop() {
        setQueueComplete();
        return true;
    }

    public AbstractEnqueue<E> filter(Predicate<E> predicate) {
        return new FilteringEnqueue(this, predicate);
    }

    public abstract AbstractDequeue<E> dequeue();

    public boolean isFull() {
        return this.remainingCapacity() == 0;
    }

    public boolean isEmpty() {
        return this.size() == 0;
    }

    public String getQueueName() {
        return queueName;
    }

    public AbstractEnqueue<E> setQueueName(String name) {
        this.queueName = name;
        return this;
    }

    public abstract void clear();

    public abstract int remainingCapacity();

    public abstract int size();

    public abstract Object getNotifier();

    public abstract void setNotifier(Object notifier);

    public abstract boolean getQueueCanceled();

    public abstract void setQueueCanceled(boolean flag);

    public abstract void setQueueComplete();

    public abstract boolean getQueueComplete();

    public abstract E remove(final Object o);


    /**
     * Inserts the specified element into this queue if it is possible to do
     * so immediately without violating capacity restrictions, returning
     * {@code true} upon success and throwing an
     * {@code IllegalStateException} if no space is currently available.
     * When using a capacity-restricted queue, it is generally preferable to
     * use {@link #offer(Object) offer}.
     *
     * @param e the element to put
     * @return {@code true} (as specified by {@link Collection#add})
     * @throws IllegalStateException if the element cannot be added at this
     *         time due to capacity restrictions
     * @throws ClassCastException if the class of the specified element
     *         prevents it from being added to this queue
     * @throws NullPointerException if the specified element is null
     * @throws IllegalArgumentException if some property of the specified
     *         element prevents it from being added to this queue
     */
    /**
     * Inserts the specified element into this queue if it is possible to do
     * so immediately without violating capacity restrictions, returning
     * {@code true} upon success and throwing an
     * {@code IllegalStateException} if no space is currently available.
     * When using a capacity-restricted queue, it is generally preferable to
     * use {@link #offer(Object) offer}.
     *
     * @param e the element to put
     * @return {@code true} (as specified by {@link Collection#add})
     * @throws IllegalStateException    if the element cannot be added at this
     *                                  time due to capacity restrictions
     * @throws ClassCastException       if the class of the specified element
     *                                  prevents it from being added to this queue
     * @throws NullPointerException     if the specified element is null
     * @throws IllegalArgumentException if some property of the specified
     *                                  element prevents it from being added to this queue
     */
    public abstract boolean add(E e);


    /**
     * Inserts the specified element into this queue if it is possible to do
     * so immediately without violating capacity restrictions, returning
     * {@code true} upon success and {@code false} if no space is currently
     * available.  When using a capacity-restricted queue, this method is
     * generally preferable to {@link #add}, which can fail to insert an
     * element only by throwing an exception.
     *
     * @param e the element to put
     * @return {@code true} if the element was added to this queue, else
     * {@code false}
     * @throws ClassCastException       if the class of the specified element
     *                                  prevents it from being added to this queue
     * @throws NullPointerException     if the specified element is null
     * @throws IllegalArgumentException if some property of the specified
     *                                  element prevents it from being added to this queue
     */
    public abstract boolean offer(E e);

    /**
     * Inserts the specified element into this queue, waiting if necessary
     * for space to become available.
     *
     * @param e the element to put
     * @throws InterruptedException     if interrupted while waiting
     * @throws ClassCastException       if the class of the specified element
     *                                  prevents it from being added to this queue
     * @throws NullPointerException     if the specified element is null
     * @throws IllegalArgumentException if some property of the specified
     *                                  element prevents it from being added to this queue
     */
    public abstract void put(E e) throws InterruptedException;

    /**
     * Inserts the specified element into this queue, waiting up to the
     * specified wait time if necessary for space to become available.
     *
     * @param e       the element to put
     * @param timeout how long to wait before giving up, in units of
     *                {@code unit}
     * @param unit    a {@code TimeUnit} determining how to interpret the
     *                {@code timeout} parameter
     * @return {@code true} if successful, or {@code false} if
     * the specified waiting time elapses before space is available
     * @throws InterruptedException     if interrupted while waiting
     * @throws ClassCastException       if the class of the specified element
     *                                  prevents it from being added to this queue
     * @throws NullPointerException     if the specified element is null
     * @throws IllegalArgumentException if some property of the specified
     *                                  element prevents it from being added to this queue
     */
    public abstract boolean offer(E e, long timeout, TimeUnit unit)
            throws InterruptedException;
}
