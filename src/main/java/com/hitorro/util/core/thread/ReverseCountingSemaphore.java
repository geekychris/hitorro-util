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

import com.hitorro.util.core.Log;


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
