package ht.util.core.queue;

/**
 * Copyright (c) 2003 - present HiTorro All rights reserved. User: chris Date: Mar 9, 2005 Time: 4:05:27 PM
 */
public interface Queue<E> {
    E dequeue() throws ThreadedQueueCanceledException,
            ThreadedQueueTimeoutException;

    E peek() throws ThreadedQueueCanceledException,
            ThreadedQueueTimeoutException;

    void enqueue(E e) throws ThreadedQueueCanceledException,
            ThreadedQueueTimeoutException;

    int getCount();

    boolean isEmpty();

    boolean isFull();

    void clear();
    /**
     * How much can the queue hold.
     *
     * @return capacity or -1 if unconstrained
     */
    int capacity ();
}
