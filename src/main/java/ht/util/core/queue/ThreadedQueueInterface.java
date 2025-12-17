package ht.util.core.queue;

import java.util.concurrent.BlockingQueue;

/**
 *
 */
public interface ThreadedQueueInterface<E> extends BlockingQueue<E> {
    boolean isEmpty();

    boolean isFull();

    Object getNotifier();

    void setNotifier(Object notifier);

    boolean getQueueCanceled();

    void setQueueCanceled(boolean flag);

    void setQueueComplete();

    boolean getQueueComplete();

    String getQueueName();
}
