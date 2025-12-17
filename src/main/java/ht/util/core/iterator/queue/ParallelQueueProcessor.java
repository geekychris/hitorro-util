package ht.util.core.iterator.queue;

public class ParallelQueueProcessor<E, O> {
    protected long timeout = 1000;
    AbstractDequeue<E> dequeue;
    AbstractEnqueue<O> targetEnqueue;

    ParallelQueueProcessor(AbstractDequeue<E> dequeue, AbstractEnqueue<O> targetEnqueue) {
        this.dequeue = dequeue;
        this.targetEnqueue = targetEnqueue;
    }

    public boolean running() {
        return (!dequeue.isCompleted() && dequeue.size() > 0) | dequeue.getQueueCanceled();
    }

    public long getTimeoutMillis() {
        return timeout;
    }
}
