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

import com.hitorro.util.core.string.Fmt;

import java.util.concurrent.TimeUnit;
import java.util.function.Function;

public class DequeueParThread<E, O, T> extends Thread {
    protected Function<E, O> mapper;
    private ParallelQueueProcessor<E, O> processor = null;
    private T m_threadData;
    private boolean completed = false;

    private DequeueParThread() {
    }

    public DequeueParThread(ThreadGroup group, ParallelQueueProcessor<E, O> processor, String name,
                            int threadNumber, Function<E, O> mapper) {
        super(group, Fmt.S("QProcessorThread: %s-%s", name, Integer
                .toString(threadNumber)));
        this.processor = processor;
        this.mapper = mapper;
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

        while (processor.running()) {

            E work = null;
            try {
                work = processor.dequeue.poll(processor.getTimeoutMillis(), TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {

            }

            O o = mapper.apply(work);

            if (o != null && processor.targetEnqueue != null) {
                try {
                    processor.targetEnqueue.put(o);
                } catch (InterruptedException e) {
                }
            } else {
            }

        }
        completed = true;
    }
}