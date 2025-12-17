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
package com.hitorro.util.core.thread.farm.jobfarm;

import com.hitorro.util.core.Env;
import com.hitorro.util.core.iterator.queue.AbstractEnqueue;
import com.hitorro.util.core.thread.EnhancedThreadGroup;
import com.hitorro.util.core.thread.farm.Farm;
import com.hitorro.util.core.thread.farm.FarmCommand;
import com.hitorro.util.core.thread.farm.FarmSink;

/**
 * Responsible for taking a "job" at a time and using an implementation of the "JobFarmEnqueueAgent" to populate the
 * inputqueue of this single step Farm.  Results are collected at the bottom.
 * <p/>
 * in construction you provide the JobFarmEnqueueAgent and the farm command to execute.  The first tells you how to
 * put things the second how to process those things that are enqueued. The JobFarmEnqueueAgent must provide a
 * container to place the results in before handing them back via the invocation of "execute"
 */
public class JobFarm<ELEMENT, RESULT, JOB> {
    private static EnhancedThreadGroup JobPipelineGroup = new EnhancedThreadGroup("JobFarmGroup");
    private FarmCommand<JobFarmElement<ELEMENT, RESULT, JOB>, JobFarmElement<ELEMENT, RESULT, JOB>, Object> fc;
    private int inputQueueLength;
    private int workerThreadCount;
    private JobFarmEnqueueAgent<JOB, ELEMENT, RESULT> ea;
    private FarmSink sink;
    private JobFarmSink<ELEMENT, RESULT, JOB> sinkCommand = new JobFarmSink();
    private AbstractEnqueue<JobFarmElement<ELEMENT, RESULT, JOB>> inQueue = null;

    private AbstractEnqueue<JobFarmElement<ELEMENT, RESULT, JOB>> outQueue = null;
    private Farm<FarmCommand<JobFarmElement<ELEMENT, RESULT, JOB>, JobFarmElement<ELEMENT, RESULT, JOB>, Object>, FarmCommand<JobFarmElement<ELEMENT, RESULT, JOB>,
            JobFarmElement<ELEMENT, RESULT, JOB>, Object>, Object> farm;

    public JobFarm(JobFarmEnqueueAgent<JOB, ELEMENT, RESULT> ea, int inputQueueLength, int workerThreadCount, FarmCommand<JobFarmElement<ELEMENT, RESULT, JOB>, JobFarmElement<ELEMENT, RESULT, JOB>, Object> fc) {
        this.fc = fc;
        this.ea = ea;
        this.workerThreadCount = workerThreadCount;
        this.inputQueueLength = inputQueueLength;
        initFarm();
    }

    /**
     * Execute the job usint the provided JobFarmEnqueueAgent until there is no more items in the queue
     *
     * @param job
     * @return
     */
    public synchronized JobResultContainer execute(JOB job) {
        JobResultContainer container = ea.getResultContainer();

        sinkCommand.setResultContainer(container, ea, job);
        int processed = ea.executeEnqueue(job, inQueue);
        while (farm.hasItemsInPipeline() && container.getProcessedCount() < processed) {
            // currently we never exit this. until complete...you better get your math right in the EnqueAgent!!!
            Env.sleepMillis(50);
        }
        return container;
    }

    private void initFarm() {
        inQueue = AbstractEnqueue.arrayBlocking(inputQueueLength);
        outQueue = AbstractEnqueue.arrayBlocking(inputQueueLength);

        farm = new Farm("JobFarm", JobPipelineGroup, inQueue, outQueue, fc, workerThreadCount);
        farm.start();
        sink = new FarmSink("PullAndForwadBackfill", JobPipelineGroup, outQueue, sinkCommand);
        sink.start();
    }
}
