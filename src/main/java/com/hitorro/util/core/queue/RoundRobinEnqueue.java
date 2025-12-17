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

import com.fasterxml.jackson.databind.JsonNode;
import com.hitorro.util.core.BooleanUtil;
import com.hitorro.util.core.Log;
import com.hitorro.util.core.iterator.queue.AbstractDequeue;
import com.hitorro.util.core.iterator.queue.AbstractEnqueue;

import java.io.IOException;
import java.util.concurrent.TimeUnit;


public class RoundRobinEnqueue<E> extends AbstractEnqueue<E> {
    private AbstractEnqueue<E> queues[];
    private int priorities[];
    private boolean completed[];
    private int index = 0;
    private boolean canceled = false;
    private boolean complete = false;
    private int queueEnqueueSlots;
    private int channels;

    private String name;

    private Object notifier = new Object();

    public RoundRobinEnqueue(String name, AbstractEnqueue<E> queues[], int priorities[]) {
        this.name = name;
        this.queues = queues;
        this.priorities = priorities;
        channels = priorities.length;
        this.completed = BooleanUtil.getBooleanArray(channels, false);
        setCount();
        for (AbstractEnqueue q : queues) {
            q.setNotifier(notifier);
        }
    }

    public Object getNotifier() {
        return notifier;
    }

    public void setNotifier(Object notifier) {
        this.notifier = notifier;
    }

    AbstractEnqueue<E>[] getEnqueues() {
        return queues;
    }

    public void put(E obj) {
        synchronized (notifier) {
            try {
                // scan for any queue not full
                while (isFull()) {
                    if (canceled) {
                        throw new ThreadedQueueCanceledException();
                    }
                    notifier.wait();
                }
                while (true) {

                    if (queueEnqueueSlots > 0 && queues[index].remainingCapacity() > 0) {
                        enq(obj);
                        notifier.notifyAll();
                        return;
                    } else {
                        if (advance(true)) {
                            enq(obj);
                            return;
                        }
                    }
                    wait();
                }
            } catch (InterruptedException e) {
            }
        }
    }

    @Override
    public boolean offer(final E e, final long timeout, final TimeUnit unit) throws InterruptedException {
        return false;
    }

    private void enq(final E obj) throws InterruptedException {
        queues[index].put(obj);
        queueEnqueueSlots--;
        notifier.notify();
    }

    @Override
    public AbstractDequeue<E> dequeue() {
        // not implemented as would return multiple values.
        return null;
    }

    @Override
    public void clear() {
        for (AbstractEnqueue ae : queues) {
            ae.clear();
        }
    }

    @Override
    public int remainingCapacity() {
        int count = 0;
        for (AbstractEnqueue ae : queues) {
            count += ae.remainingCapacity();
        }
        return count;
    }

    synchronized public int size() {
        int count = 0;
        for (AbstractEnqueue ae : queues) {
            count += ae.size();
        }
        return count;
    }

    public boolean getQueueCanceled() {
        return canceled;
    }

    public void setQueueCanceled(boolean flag) {
        for (AbstractEnqueue q : queues) {
            q.setQueueCanceled(true);
        }
        canceled = true;
    }

    public void setQueueComplete() {
        for (AbstractEnqueue q : queues) {
            q.setQueueComplete();
        }
        complete = true;
    }

    public boolean getQueueComplete() {
        return complete;
    }

    public boolean isEmpty() {
        for (int i = index; i < queues.length + index; i++) {
            int index = i % queues.length;
            if (!completed[index]) {
                if (queues[index].size() == 0) {
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
                if (queues[index].remainingCapacity() != 0) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public E remove(final Object o) {
        Log.util.fatal("remove should not be used in RoundRobinEnqueue");
        return null;
    }

    @Override
    public boolean init(final JsonNode node) {
        return true;
    }

    @Override
    public boolean add(final E e) {
        Log.util.fatal("add should not be used in RoundRobinEnqueue");
        return false;
    }

    @Override
    public void close() throws IOException {

    }

    @Override
    public boolean offer(final E e) {
        Log.util.fatal("offer should not be used in RoundRobinEnqueue");
        return false;
    }


    private boolean advance(boolean checkForRoom) {
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
                if (queues[index].getQueueComplete()) {
                    completed[index] = true;
                    counted++;
                    continue;
                }

                if (checkForRoom && queues[index].remainingCapacity() == 0) {
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
        queueEnqueueSlots = priorities[index];
    }
}


