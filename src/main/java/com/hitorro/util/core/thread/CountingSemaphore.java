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
 * User: chris Date: Jun 23, 2004 Time: 1:52:16 PM
 * <p/>
 * Description:
 */
public class CountingSemaphore {

    /**
     * Implementation of classical Dykstra semaphores.
     * @author Ken Williams
     */


    /**
     * The counter value for this counting semaphore.  The value is initialized by the constructor and modified by p and
     * v.
     */
    private int counter;


    /**
     * Constructor to create a semaphore object.
     *
     * @param initCounter int initial value for the semaphore counter.
     */
    public CountingSemaphore(int initCounter) {
        counter = initCounter;
    }


    /**
     * The p method decrements the semaphore counter.  If the counter value is zero before being decremented, the thread
     * will be suspended until another thread increments the counter with the v method.
     */
    public synchronized void p() {
        try {
            if (counter == 0) {
                wait();
            }
        } catch (InterruptedException e) {
            Log.util.warn("Semaphore exception %s", e);
            e.printStackTrace();
        }
        counter--;
    }


    /**
     * The p method decrements the semaphore counter.  If the counter value is zero before being decremented, the thread
     * will be suspended until another thread increments the counter with the v method.
     *
     * @param timeout long If a thread is delayed for more than the number of milliseconds specified by the timeout
     *                parameter, an exception will be thrown.
     * @throws InterruptedException thrown if the thread is delayed more than timeout milliseconds.
     */


    public synchronized void p(long timeout) throws InterruptedException {
        if (counter == 0) {
            wait(timeout);
        }
        counter--;
    }

    /**
     * The v method increments the semaphore counter.  If other threads are suspended on this semaphore, one of them
     * will be activated.
     */
    public synchronized void v() {
        counter++;
        notify();
    }

    public synchronized int getCount() {
        return counter;
    }
}