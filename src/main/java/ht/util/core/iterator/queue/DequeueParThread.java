package ht.util.core.iterator.queue;

import ht.util.core.string.Fmt;

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