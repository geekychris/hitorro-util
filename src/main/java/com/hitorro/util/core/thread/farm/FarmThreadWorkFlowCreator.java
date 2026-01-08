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

import com.hitorro.util.core.iterator.queue.AbstractEnqueue;
import com.hitorro.util.core.iterator.sinks.Sink;
import com.hitorro.util.core.thread.EnhancedThreadGroup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 */
public class FarmThreadWorkFlowCreator<E, T> {
    private List<CommandStep> steps = new ArrayList<CommandStep>();

    private Map<String, AbstractEnqueue<E>> inputQueues = new HashMap();
    private Map<String, Farm<E, E, T>> farms = new HashMap();
    private FarmSink<E> sink = null;

    private Sink<E> fsCommand = null;
    private String fsName;

    private AbstractEnqueue<E> headQ = null;
    private int sinkQLength;
    private AbstractEnqueue<E> sinkQ = null;
    private ThreadGroup tg;


    public FarmThreadWorkFlowCreator(String groupName) {
        tg = new EnhancedThreadGroup(groupName);
    }

    public AbstractEnqueue<E> addStep(FarmCommand<E, E, T> commandIn, int queueLengthIn, String inputQueueName, int threadCount) {
        CommandStep<E, E, T> c = new CommandStep<E, E, T>(commandIn, queueLengthIn, inputQueueName, threadCount);
        steps.add(c);
        return c.getInputQueue();
    }

    public void setSink(Sink<E> command, String name, int inputQueueLength) {
        fsCommand = command;
        fsName = name;
        sinkQLength = inputQueueLength;
    }


    /**
     * Construct the pipeline
     *
     * @return
     */
    public AbstractEnqueue initialize() {
        if (steps.size() == 0) {
            // cant process it as there is no steps
            return null;
        }

        if (fsCommand == null) {
            // no put defined.
            return null;
        }
        sinkQ = AbstractEnqueue.arrayBlocking(sinkQLength).setQueueName(fsName);
        sink = new FarmSink(fsName, tg, sinkQ, fsCommand);
        AbstractEnqueue<E> prev = sinkQ;

        CommandStep cs;

        for (int i = steps.size() - 1; i >= 0; i--) {
            cs = steps.get(i);
            AbstractEnqueue<E> q = cs.getInputQueue();
            Farm<E, E, T> farm = cs.getFarm(prev, tg);
            farms.put(cs.getQueueName(), farm);
            // make it so we can lookup an input queue by name
            inputQueues.put(cs.getQueueName(), q);
            // start it!!!
            farm.start();
            prev = q;
        }
        headQ = steps.get(0).getInputQueue();

        sink.start();
        return headQ;
    }

    public AbstractEnqueue<E> getSinkQueue() {
        return sinkQ;
    }
}

class CommandStep<I, O, T> {
    private FarmCommand<I, O, T> command;
    private int inputQueueLength;
    private String queueName;
    private int threadCount;

    private AbstractEnqueue<I> q = null;

    public CommandStep(FarmCommand<I, O, T> commandIn, int queueLengthIn, String inputQueueName, int threadCountIn) {
        setCommand(commandIn);
        setInputQueueLength(queueLengthIn);
        setQueueName(inputQueueName);
        setThreadCount(threadCountIn);
    }

    public AbstractEnqueue<I> getInputQueue() {
        if (q == null) {
            q = AbstractEnqueue.arrayBlocking(inputQueueLength).setQueueName(queueName);
        }
        return q;
    }

    public Farm<I, O, T> getFarm(AbstractEnqueue<O> q, ThreadGroup tg) {
        // dequeue/ enqueue
        return new Farm(queueName, tg, getInputQueue(), q, command, threadCount);
    }

    public FarmCommand<I, O, T> getCommand() {
        return command;
    }

    public void setCommand(FarmCommand<I, O, T> command) {
        this.command = command;
    }

    public int getInputQueueLength() {
        return inputQueueLength;
    }

    public void setInputQueueLength(int inputQueueLength) {
        this.inputQueueLength = inputQueueLength;
    }

    public String getQueueName() {
        return queueName;
    }

    public void setQueueName(String queueName) {
        this.queueName = queueName;
    }

    public int getThreadCount() {
        return threadCount;
    }

    public void setThreadCount(int threadCount) {
        this.threadCount = threadCount;
    }
}