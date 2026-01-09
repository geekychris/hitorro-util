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

import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

/**
 * A threaded queue with timeout functionality.
 * <p/>
 * Allows the queue to be marked as canceled, that is will cause any producer or consumer that is blocked on the queue
 * to unblock if we know the peer is not going to apply / consume anymore this implies that the producers and consumers if
 * you know you programmatically cancel a queue, should check if the queue is canceled to prevent loss of data (if you
 * care). This is done by catching ThreadedQueueCanceledException
 *
 * @author chris
 */
public class ThreadedQueue<E> extends BoundedQueue<E> implements ThreadedQueueInterface<E> {
    private boolean isCanceled = false;

    private boolean isComplete = false;

    private String m_queueName = "<Name Not Set>";

    private Object notifier = new Object();

    public ThreadedQueue(int size) {
        super(size);
    }

    public ThreadedQueue(int size, String queueName) {
        super(size);
        m_queueName = queueName;
    }

    public Object getNotifier() {
        return notifier;
    }

    public void setNotifier(Object notifier) {
        this.notifier = notifier;
    }

    public int drainTo(Collection<? super E> c) {
        int count = 0;
        while (!isEmpty()) {
            c.add(take());
            count++;
        }
        return count;
    }

    public int drainTo(Collection<? super E> c, int maxElements) {
        int count = 0;
        while (!isEmpty() && count < maxElements) {
            c.add(take());
            count++;
        }
        return count;
    }

    public E take() {
        synchronized (notifier) {
            try {
                while (isEmpty()) {
                    notifier.wait();
                }
            } catch (InterruptedException e) {
            }
            E result = super.dequeue();
            notifier.notify();
            return result;
        }
    }


    public E peek() {
        synchronized (notifier) {
            try {
                while (isEmpty()) {
                    notifier.wait();
                }
            } catch (InterruptedException e) {
            }
            E result = super.peek();
            notifier.notify();
            return result;
        }
    }

    /**
     * If we wait beyond our timeout we return a null object to indicate this thing looks like a dead chicken.
     *
     * @param timeout
     * @param unit
     * @return
     */
    public E poll(long timeout, final TimeUnit unit) {
        synchronized (notifier) {
            try {
                long entryTime = System.currentTimeMillis();
                while (isEmpty()) {
                    notifier.wait(timeout);
                    long elapsed = System.currentTimeMillis() - entryTime;
                    if (elapsed > timeout) {
                        // we timed out and we should return null;
                        return null;
                    }
                    if (isCanceled) {
                        throw new ThreadedQueueCanceledException();
                    }
                }
            } catch (InterruptedException e) {
            }
            E result = super.dequeue();
            notifier.notify();
            return result;
        }
    }

    @Override
    public int remainingCapacity() {
        return super.capacity() - super.getCount();
    }

    @Override
    public boolean remove(final Object o) {
        return false;
    }

    @Override
    public boolean containsAll(final Collection<?> c) {
        return false;
    }

    @Override
    public boolean addAll(final Collection<? extends E> c) {
        return false;
    }

    @Override
    public boolean removeAll(final Collection<?> c) {
        return false;
    }

    @Override
    public boolean retainAll(final Collection<?> c) {
        return false;
    }

    @Override
    public boolean contains(final Object o) {
        return false;
    }

    @Override
    public Iterator<E> iterator() {
        return null;
    }


    @Override
    public Object[] toArray() {
        return new Object[0];
    }


    @Override
    public <T> T[] toArray(final T[] a) {
        return null;
    }

    public boolean add(E obj) {
        synchronized (notifier) {
            if (isFull()) {
                throw new IllegalStateException();
            }
            enqueue(obj, false);
            return true;
        }
    }

    @Override
    public boolean offer(final E e) {
        return false;
    }

    @Override
    public E remove() {
        return null;
    }

    @Override
    public E poll() {
        return null;
    }

    @Override
    public E element() {
        return null;
    }

    public void put(E obj) {
        enqueue(obj, false);
    }

    @Override
    public boolean offer(final E e, final long timeout, final TimeUnit unit) throws InterruptedException {
        return false;
    }

    public boolean enqueue(E obj, boolean failOnFull)
            throws ThreadedQueueCanceledException,
            ThreadedQueueTimeoutException {
        synchronized (notifier) {
            try {
                while (isFull()) {
                    if (failOnFull) {
                        return false;
                    }
                    if (isCanceled) {
                        throw new ThreadedQueueCanceledException();
                    }
                    notifier.wait();
                }
            } catch (InterruptedException e) {
            }
            super.enqueue(obj);
            notifier.notify();
        }
        return true;
    }

    public int size() {
        synchronized (notifier) {
            return super.getCount();
        }
    }

    public boolean getQueueCanceled() {
        return isCanceled;
    }

    public void setQueueCanceled(boolean flag) {
        isCanceled = flag;
    }

    /**
     * Setting this implies that if you do a get and there is nothing in the queue, see if the queue has been completed,
     * if it has, there will be no more data and should shut down.
     */
    public void setQueueComplete() {
        isComplete = true;
    }

    public boolean getQueueComplete() {
        return isComplete;
    }

    public String getQueueName() {
        return m_queueName;
    }

    public boolean isEmpty() {
        synchronized (notifier) {
            return super.isEmpty();
        }
    }

    public boolean isFull() {
        synchronized (notifier) {
            return super.isFull();
        }
    }
}
