/*
 * Copyright (c) 2006-2025 Chris Collins
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.hitorro.util.core.thread;

import com.hitorro.util.core.string.Fmt;

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