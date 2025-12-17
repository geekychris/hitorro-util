package ht.util.core.thread;

import ht.util.core.Log;


/**
 * Copyright (c) HiTorro 2003-2008, Inc.
 * <p/>
 * User: chris Date: Jun 23, 2004 Time: 5:23:18 PM
 * <p/>
 * Description:
 */
/*
  Allow users of a resource to increment the counter.
  Once finished they decrement.
  waitForZero is called by some kind of queue generator, knowing the queue is work
  queue is empty already
*/

class ReverseCountingSemaphore {
    private int m_count;

    public synchronized void increment() {
        m_count++;
    }

    public synchronized void decrement() {
        if (m_count > 0) {
            m_count--;
        } else {
            Log.util.warn("Semaphore exception, should " +
                    "not be able to decrement below zero resource usage");
        }
        notify();
    }

    public synchronized void waitForZero() {
        try {
            while (m_count > 0) {
                wait();
            }
        } catch (InterruptedException ie) {
            Log.util.warn("Semaphore exception");
            ie.printStackTrace();
        }
    }
}
