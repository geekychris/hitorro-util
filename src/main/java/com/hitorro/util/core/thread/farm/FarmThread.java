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
package com.hitorro.util.core.thread.farm;

import com.hitorro.util.core.iterator.queue.AbstractDequeue;
import com.hitorro.util.core.iterator.queue.AbstractEnqueue;
import com.hitorro.util.core.queue.ThreadedQueueCanceledException;
import com.hitorro.util.core.queue.ThreadedQueueTimeoutException;
import com.hitorro.util.core.string.Fmt;

import java.util.concurrent.TimeUnit;

/**
 * @param <I> Input Queue Type
 * @param <O> Output Queue Type
 * @param <T> Type for thread local storage
 * @author CCOLLINS
 */
public class FarmThread<I, O, T> extends Thread {
    private Farm<I, O, T> m_farm = null;

    private T m_threadData;

    private boolean completed = false;

    private FarmThread() {
    }

    public FarmThread(ThreadGroup group, Farm<I, O, T> farm, String name,
                      int threadNumber) {
        super(group, Fmt.S("QProcessorThread: %s-%s", name, Integer
                .toString(threadNumber)));
        m_farm = farm;
        this.setDaemon(true);
    }

    public boolean isCompleted() {
        return completed;
    }

    /**
     * Get the thread local data.
     *
     * @return
     */
    public T getThreadData() {
        return m_threadData;
    }

    /**
     * Set the thread local data.
     *
     * @param data
     */
    public void setThreadData(T data) {
        m_threadData = data;
    }

    public void run() {
        AbstractEnqueue<O> tq = m_farm.getOutputQueue();
        AbstractDequeue<O> td = tq.dequeue();
        while (m_farm.running()) {
            try {
                I work = null;
                try {
                    work = m_farm.getInputQueue().dequeue().poll(m_farm.getTimeoutMillis(), TimeUnit.MILLISECONDS);
                } catch (InterruptedException e) {

                }
                if (work == null) {
                    continue;
                }
                O o = m_farm.produce(work);

                if (o != null && tq != null) {
                    try {
                        m_farm.getOutputQueue().put(o);
                    } catch (InterruptedException e) {
                    }
                } else {
                    // Log.util.error("FarmThread got null object from apply
                    // method");
                }
            } catch (ThreadedQueueTimeoutException timeoutException) {
                // Log.threads.debug("Timed out working on
                // QueueProcessorThread...waiting again");
                if (m_farm.getInputQueue().getQueueComplete()) {
                    // we have a complete queue, we should exit but
                    m_farm.notifyThreadExitDueToQueueComplete();
                    break;
                }
            } catch (ThreadedQueueCanceledException timeoutException) {

            }
        }
        if (m_farm.getInputQueue().getQueueCanceled()) {
            m_farm.rollback();
        } else {
            if (m_farm.getInputQueue().getQueueComplete()) {
                m_farm.commit();
            }
        }
        completed = true;
    }
}