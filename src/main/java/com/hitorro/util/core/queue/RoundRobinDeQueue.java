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

import com.hitorro.util.core.BooleanUtil;
import com.hitorro.util.core.Log;
import com.hitorro.util.core.iterator.queue.AbstractDequeue;
import com.hitorro.util.core.iterator.queue.AbstractEnqueue;

import java.util.Collection;
import java.util.concurrent.TimeUnit;

/**
 * Uses a weighted round robin mechanism to allow multiple threaded queues to be read.  The idea is that you can specify
 * a number of slots to be taken from a particular queue.  When dequeueing, the max of n elements can be taken from a
 * particular channel before moving onto the next.  If that channel has less than n, the RRQ moves onto the next
 * earlier
 */
public class RoundRobinDeQueue<E> extends AbstractDequeue<E> {
    private AbstractDequeue<E> queues[];
    private int priorities[];
    private int currCounts[];
    private boolean completed[];
    private int index = 0;
    private boolean canceled = false;
    private boolean complete = false;
    private int count;
    private int channels;

    private String name;

    private Object notifier = new Object();
    private RoundRobinEnqueue<E> enqueue;

    public RoundRobinDeQueue(String name, RoundRobinEnqueue<E> enqueue, int priorities[]) {
        this.name = name;
        this.enqueue = enqueue;
        this.notifier = enqueue.getNotifier();
        AbstractEnqueue<E> arr[] = enqueue.getEnqueues();
        this.queues = new AbstractDequeue[arr.length];
        for (int i = 0; i < arr.length; i++) {
            queues[i] = arr[i].dequeue();
        }
        this.priorities = priorities;
        channels = priorities.length;
        this.currCounts = new int[channels];
        this.completed = BooleanUtil.getBooleanArray(channels, false);

        this.notifier = queues[0].getEnqueue().getNotifier();
        for (AbstractDequeue q : queues) {
            if (q.getEnqueue().getNotifier() != this.notifier) {
                Log.util.fatal("RoundRobinDeQueue requies all input queues to share their notifier object");
            }
        }
        setCount();
    }

    public Object getNotifier() {
        return notifier;
    }

    public void setNotifier(Object notifier) {
        this.notifier = notifier;
    }

    private boolean advance(boolean stopOnNotEmpty) {
        int counted = 0;
        while (counted < channels) {
            index = index + 1;

            if (index >= priorities.length) {
                index = 0;
            }
            if (completed[index] == false) {
                if (queues[index].getQueueCanceled()) {
                    // we are done, a single canceled channel is considered some kind of downstream
                    // rollback
                    canceled = true;
                    return false;
                }
                if (queues[index].getEnqueue().getQueueComplete()) {
                    completed[index] = true;
                    counted++;
                    continue;
                }
                if (stopOnNotEmpty && queues[index].size() == 0) {
                    counted++;
                    continue;
                }
                setCount();
                return true;
            } else {
                // skip this guy, keep looking
                counted++;
            }
        }
        /// more channels
        complete = true;
        return false;
    }

    private void setCount() {
        count = priorities[index];
    }

    synchronized public E take() throws InterruptedException {
        return poll(1000000000000000l, TimeUnit.MILLISECONDS);
    }

    synchronized public E peek() throws ThreadedQueueCanceledException, ThreadedQueueTimeoutException {
        return null;
    }

    public E poll(long timeout, final TimeUnit unit) throws InterruptedException {
        synchronized (notifier) {
            try {
                long entryTime = System.currentTimeMillis();

                while (waitingForValue()) {
                    notifier.wait(1000);
                    long elapsed = System.currentTimeMillis() - entryTime;
                    if (elapsed > timeout) {
                        // we timed out and we should return null;
                        throw new ThreadedQueueTimeoutException();
                    }
                    if (canceled) {
                        throw new ThreadedQueueCanceledException();
                    }
                }
            } catch (InterruptedException e) {
            }


            E e = queues[index].poll(timeout, unit);
            if (e != null) {
                count--;
                notifier.notify();
                return e;
            }
            return null;
        }
    }

    @Override
    public int remainingCapacity() {
        int count = 0;
        for (AbstractDequeue ad : queues) {
            count += ad.remainingCapacity();
        }
        return count;
    }

    @Override
    public int drainTo(final Collection<? super E> c) {
        int count = 0;
        while (this.remainingCapacity() > 0) {
            try {
                count++;
                c.add(take());
            } catch (InterruptedException e) {

            }
        }
        return count;
    }

    @Override
    public int drainTo(final Collection<? super E> c, final int maxElements) {
        int count = 0;
        while (this.remainingCapacity() > 0 && count < maxElements) {
            try {
                count++;
                c.add(take());
            } catch (InterruptedException e) {

            }
        }
        return count;
    }

    private boolean waitingForValue() {
        if (count > 0) {
            // will take from current queue if it has something
            if (queues[index].size() > 0) {
                return false;
            }
        }
        return !advance(true);
    }

    public void put(E obj) throws ThreadedQueueTimeoutException, ThreadedQueueCanceledException {
        //not implemented
    }

    public int size() {
        synchronized (notifier) {
            int count = 0;
            for (int i = 0; i < queues.length; i++) {
                if (!completed[i]) {
                    count += queues[i].size();
                }
            }
            return count;
        }
    }

    @Override
    public boolean isCompleted() {
        for (boolean b : completed) {
            if (!b) {
                return false;
            }
        }
        return true;
    }

    public boolean getQueueCanceled() {
        return canceled;
    }

    public void setQueueCanceled(boolean flag) {

    }

    public void setQueueComplete() {
        complete = true;
    }

    public boolean getQueueComplete() {
        return complete;
    }

    public String getQueueName() {
        return name;
    }

    public boolean isEmpty() {
        for (int i = index; i < queues.length + index; i++) {
            int index = i % queues.length;
            if (!completed[index]) {
                if (queues[index].size() > 0) {
                    return false;
                }
            }
        }
        return true;
    }

    public boolean isFull() {
        for (int i = index; i < queues.length + index; i++) {
            int index = i % queues.length;
            if (!completed[index]) {
                if (queues[index].remainingCapacity() > 0) {
                    return false;
                }
            }
        }
        return true;
    }
}

