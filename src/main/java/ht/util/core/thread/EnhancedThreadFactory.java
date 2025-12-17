package ht.util.core.thread;

import ht.util.core.string.Fmt;

import java.util.concurrent.ThreadFactory;

/**
 * Thread factory that is used by executors. Creates threads belonging to a specific thread group and uses a common
 * thread naming convention with a counter.
 *
 * @author chris
 */
public class EnhancedThreadFactory implements ThreadFactory {
    private EnhancedThreadGroup m_tg;

    private String m_threadNameFormatString;

    private int m_counter = 0;

    private boolean m_runAsDaemon;

    private int m_priority = Thread.NORM_PRIORITY;

    public EnhancedThreadFactory(String threadGroupName,
                                 String threadNameFormatString, boolean runAsDaemon) {
        init(threadGroupName, threadNameFormatString, runAsDaemon,
                Thread.NORM_PRIORITY);
    }

    public EnhancedThreadFactory(String threadGroupName,
                                 String threadNameFormatString, boolean runAsDaemon,
                                 int threadPriority) {
        init(threadGroupName, threadNameFormatString, runAsDaemon,
                threadPriority);
    }

    private void init(String threadGroupName, String threadNameFormatString,
                      boolean runAsDaemon, int priority) {
        m_tg = new EnhancedThreadGroup(threadGroupName);
        m_threadNameFormatString = threadNameFormatString;
        m_runAsDaemon = runAsDaemon;
        m_priority = priority;
    }

    public Thread newThread(Runnable runner) {
        Thread t = new HTThread(m_tg, runner, Fmt.S(m_threadNameFormatString,
                m_counter++));
        t.setDaemon(m_runAsDaemon);
        t.setPriority(m_priority);
        return t;
    }
}