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
import com.hitorro.util.core.iterator.sinks.Sink;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Sink for a generified threaded queue, knows how to shutdown once the queue is complete.
 *
 * @param <I>
 * @author chris
 */
public class FarmSink<I> implements Runnable {
    private long timeout = 10 * 1000;

    private ThreadGroup group;

    private AbstractEnqueue<I> queue;
    private AbstractDequeue<I> dequeue;

    private Sink<I> command;

    private String name;

    private Thread thread;

    private boolean completed = false;

    private int processedItemCount = 0;

    public FarmSink(String name, ThreadGroup group, AbstractEnqueue<I> in,
                    Sink<I> command) {
        this.command = command;
        this.group = group;
        this.name = name;
        this.queue = in;
        this.dequeue = (AbstractDequeue<I>) queue.dequeue();
    }

    public void start() {
        thread = new Thread(group, this, name);
        thread.setDaemon(true);
        thread.start();
    }

    public void stop() {
        thread = null;
    }

    public boolean getCompleted() {
        return completed;
    }

    public int getProcessedCount() {
        return processedItemCount;
    }

    public void run() {
        while (!queue.getQueueComplete()) {
            try {
                I i = dequeue.poll(timeout, TimeUnit.MILLISECONDS);
                if (i != null) {
                    command.accept(i);
                    processedItemCount++;
                }
            } catch (InterruptedException e) {

            }
        }
        try {
            command.close();
        } catch (IOException e) {

        }
        completed = true;
    }
}
