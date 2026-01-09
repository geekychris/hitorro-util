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
    private long millisWait;
    private boolean m_loop;
    private TimerCallback m_callback;

    public ThreadTimer(TimerCallback callback, long millis, boolean loop) {
        m_loop = loop;
        m_callback = callback;
        millisWait = millis;
    }

    public void run() {
        while (m_loop) {
            try {
                Thread.sleep(millisWait);
            } catch (InterruptedException ie) {
                // XX Interrupted but lets still go on.
            }
            if (!m_callback.callback()) {
                break;
            }
        }
    }
}
