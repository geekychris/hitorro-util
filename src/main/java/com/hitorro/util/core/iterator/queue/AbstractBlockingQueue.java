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

import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

public abstract class AbstractBlockingQueue<E> implements BlockingQueue<E> {
    protected BlockingQueue<E> queue;

    public AbstractBlockingQueue(BlockingQueue<E> queue) {
        this.queue = queue;
    }

    public AbstractBlockingQueue filter(Predicate<E> filter) {
        return new FilterBlockingQueue(this, filter);
    }

    @Override
    public boolean add(final E e) {
        return queue.add(e);
    }

    @Override
    public boolean offer(final E e) {
        return queue.offer(e);
    }

    @Override
    public E remove() {
        return queue.remove();
    }

    @Override
    public E poll() {
        return queue.poll();
    }

    @Override
    public E element() {
        return queue.element();
    }

    @Override
    public E peek() {
        return queue.peek();
    }

    @Override
    public void put(final E e) throws InterruptedException {
        queue.put(e);
    }

    @Override
    public boolean offer(final E e, final long timeout, final TimeUnit unit) throws InterruptedException {
        return queue.offer(e, timeout, unit);
    }

    @Override
    public E take() throws InterruptedException {
        return queue.take();
    }

    @Override
    public E poll(final long timeout, final TimeUnit unit) throws InterruptedException {
        return queue.poll(timeout, unit);
    }

    @Override
    public int remainingCapacity() {
        return queue.remainingCapacity();
    }

    @Override
    public boolean remove(final Object o) {
        return queue.remove(o);
    }

    @Override
    public boolean containsAll(final Collection<?> c) {
        return queue.containsAll(c);
    }

    @Override
    public boolean addAll(final Collection<? extends E> c) {
        return queue.addAll(c);
    }

    @Override
    public boolean removeAll(final Collection<?> c) {
        return queue.removeAll(c);
    }

    @Override
    public boolean retainAll(final Collection<?> c) {
        return queue.retainAll(c);
    }

    @Override
    public void clear() {
        queue.clear();
    }

    @Override
    public int size() {
        return queue.size();
    }

    @Override
    public boolean isEmpty() {
        return queue.isEmpty();
    }

    @Override
    public boolean contains(final Object o) {
        return queue.contains(o);
    }

    @Override
    public Iterator<E> iterator() {
        return queue.iterator();
    }

    @Override
    public Object[] toArray() {
        return queue.toArray();
    }

    @Override
    public <T> T[] toArray(final T[] a) {
        return queue.toArray(a);
    }

    @Override
    public int drainTo(final Collection<? super E> c) {
        return queue.drainTo(c);
    }

    @Override
    public int drainTo(final Collection<? super E> c, final int maxElements) {
        return queue.drainTo(c, maxElements);
    }
}
