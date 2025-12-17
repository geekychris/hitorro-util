package ht.util.core.thread;

/**
 * Copyright (c) HiTorro 2003-2008, Inc.
 * <p/>
 * User: chris Date: Jul 7, 2004 Time: 2:55:30 PM
 * <p/>
 * Description:
 * <p/>
 * Timer that you setup with a time to wait and then invokes a callback handler
 */
public class ThreadTimer extends Thread {
    private long m_millisWait;
    private boolean m_loop;
    private TimerCallback m_callback;

    public ThreadTimer(TimerCallback callback, long millis, boolean loop) {
        m_loop = loop;
        m_callback = callback;
        m_millisWait = millis;
    }

    public void run() {
        while (m_loop) {
            try {
                Thread.sleep(m_millisWait);
            } catch (InterruptedException ie) {
                // XX Interrupted but lets still go on.
            }
            if (!m_callback.callback()) {
                break;
            }
        }
    }
}
