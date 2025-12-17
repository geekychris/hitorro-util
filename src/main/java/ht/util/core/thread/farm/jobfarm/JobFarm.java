package ht.util.core.thread.farm.jobfarm;

import ht.util.core.Env;
import ht.util.core.iterator.queue.AbstractEnqueue;
import ht.util.core.thread.EnhancedThreadGroup;
import ht.util.core.thread.farm.Farm;
import ht.util.core.thread.farm.FarmCommand;
import ht.util.core.thread.farm.FarmSink;

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
