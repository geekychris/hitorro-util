package ht.util.core.thread.farm.jobfarm;

import ht.util.core.iterator.queue.AbstractEnqueue;

/**
 * Something that takes a job and enqueues onto the queue
 */
public interface JobFarmEnqueueAgent<JOB, ELEMENT, RESULT> {
    int executeEnqueue(JOB job, AbstractEnqueue<JobFarmElement<ELEMENT, RESULT, JOB>> queue);

    JobResultContainer<RESULT> getResultContainer();
}
