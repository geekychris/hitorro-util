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

import com.hitorro.util.core.thread.EnhancedThreadGroup;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class ParallelDequeue<E,O, T> {

    private List<DequeueParThread<E,O,T>> threads = new ArrayList<>();
    private ParallelQueueProcessor<E,O> dqp;
    private Function<E, O> mapper;
    private ThreadGroup tg;
    private String name;
    private int threadCount;

    public ParallelDequeue (AbstractDequeue<E> dequeue, AbstractEnqueue<O> targetEnqueue,
                            Function<E, O> mapper, String name, int threadCount) {
        this.dqp = new ParallelQueueProcessor<E,O>(dequeue, targetEnqueue);
        this.mapper = mapper;
        this.name = name;
        this.threadCount = threadCount;
        tg = new EnhancedThreadGroup(name);
    }

    public void startThreads () {
        for (int i = 0; i < threadCount; i++) {
            DequeueParThread<E,O,T> dpt = new DequeueParThread<>(tg, dqp, name, i, mapper);
            threads.add(dpt);
            dpt.start();
        }
    }

}
