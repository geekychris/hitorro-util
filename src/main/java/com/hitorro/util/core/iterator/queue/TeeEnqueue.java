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

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class TeeEnqueue<E> extends AbstractEnqueue<E> {
    protected AbstractEnqueue<E> a;
    protected AbstractEnqueue<E> b;

    public TeeEnqueue(AbstractEnqueue<E> a, AbstractEnqueue<E> b) {
        this.a = a;
        this.b = b;
    }

    @Override
    public boolean init(final JsonNode node) {
        return true;
    }

    @Override
    public boolean add(final E e) {
        return a.add(e) | b.add(e);
    }

    @Override
    public void close() throws IOException {
        a.close();
        b.close();
    }

    @Override
    public boolean offer(final E e) {
        return a.offer(e) | b.offer(e);
    }

    @Override
    public void put(final E e) throws InterruptedException {
        a.put(e);
        b.put(e);
    }

    @Override
    public boolean offer(final E e, final long timeout, final TimeUnit unit) throws InterruptedException {
        return a.offer(e, timeout, unit) | b.offer(e, timeout, unit);
    }

    @Override
    public void clear() {
        a.clear();
        b.clear();
    }

    public int size() {
        return a.size() + b.size();
    }

    @Override
    public AbstractDequeue<E> dequeue() {
        return a.dequeue();
    }

    public int remainingCapacity() {
        return a.remainingCapacity() + b.remainingCapacity();
    }

    @Override
    public Object getNotifier() {
        return a.getNotifier();
    }

    @Override
    public void setNotifier(final Object notifier) {
        a.setNotifier(notifier);
        b.setNotifier(notifier);
    }

    @Override
    public boolean getQueueCanceled() {
        return a.getQueueCanceled() | b.getQueueCanceled();
    }

    @Override
    public void setQueueCanceled(final boolean flag) {
        a.setQueueCanceled(flag);
        b.setQueueCanceled(flag);
    }

    @Override
    public void setQueueComplete() {
        a.setQueueComplete();
        b.setQueueComplete();
    }

    @Override
    public boolean getQueueComplete() {
        return a.getQueueComplete() | b.getQueueComplete();
    }

    @Override
    public E remove(final Object o) {
        E e = a.remove(o);
        if (e == null) {
            e = b.remove(o);
        }
        return e;
    }

}
