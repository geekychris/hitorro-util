package ht.util.core.thread.farm;

import ht.util.core.iterator.queue.AbstractDequeue;
import ht.util.core.iterator.queue.AbstractEnqueue;
import ht.util.core.queue.ThreadedQueueCanceledException;
import ht.util.core.queue.ThreadedQueueTimeoutException;
import ht.util.core.string.Fmt;

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