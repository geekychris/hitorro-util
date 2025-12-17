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
package com.hitorro.util.io.largedata.compressedstreams;

/**
 * Copyright 2004 The Apache Software Foundation
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.IOException;

/**
 * An interprocess mutex lock.
 * <p>Typical use might look like:<pre>
 * new Lock.With(directory.makeLock("my.lock")) {
 *     public Object doBody() {
 *       <i>... code to execute while locked ...</i>
 *     }
 *   }.run();
 * </pre>
 *
 * @author Doug Cutting
 * @version $Id: Lock.java,v 1.2 2005/10/25 02:48:20 ccollins Exp $
 * @see Directory#makeLock(String)
 */
public abstract class Lock {
    public static long LOCK_POLL_INTERVAL = 1000;

    /**
     * Attempts to obtain exclusive access and immediately return upon success or failure.
     *
     * @return true iff exclusive access is obtained
     */
    public abstract boolean obtain() throws IOException;

    /**
     * Attempts to obtain an exclusive lock within amount of time given. Currently polls once per second until
     * lockWaitTimeout is passed.
     *
     * @param lockWaitTimeout length of time to wait in ms
     *
     * @return true if lock was obtained
     * @throws IOException if lock wait times out or obtain() throws an IOException
     */
    public boolean obtain(long lockWaitTimeout) throws IOException {
        boolean locked = obtain();
        int maxSleepCount = (int) (lockWaitTimeout / LOCK_POLL_INTERVAL);
        int sleepCount = 0;
        while (!locked) {
            if (++sleepCount == maxSleepCount) {
                throw new IOException("Lock obtain timed out: " + this.toString());
            }
            try {
                Thread.sleep(LOCK_POLL_INTERVAL);
            } catch (InterruptedException e) {
                throw new IOException(e.toString());
            }
            locked = obtain();
        }
        return locked;
    }

    /**
     * Releases exclusive access.
     */
    public abstract void release();

    /**
     * Returns true if the resource is currently locked.  Note that one must still call {@link #obtain()} before using
     * the resource.
     */
    public abstract boolean isLocked();


    /**
     * Utility class for executing code with exclusive access.
     */
    public abstract static class With {
        private Lock lock;
        private long lockWaitTimeout;

        /**
         * Constructs an executor that will grab the named lock. Defaults lockWaitTimeout to Lock.COMMIT_LOCK_TIMEOUT.
         *
         * @deprecated Kept only to avoid breaking existing code.
         */
        public With(Lock lock) {
            this(lock, 1000 * 1000);
        }

        /**
         * Constructs an executor that will grab the named lock.
         */
        public With(Lock lock, long lockWaitTimeout) {
            this.lock = lock;
            this.lockWaitTimeout = lockWaitTimeout;
        }

        /**
         * Code to execute with exclusive access.
         */
        protected abstract Object doBody() throws IOException;

        /**
         * Calls {@link #doBody} while <i>lock</i> is obtained.  Blocks if lock cannot be obtained immediately.  Retries
         * to obtain lock once per second until it is obtained, or until it has tried ten times. Lock is released when
         * {@link #doBody} exits.
         */
        public Object run() throws IOException {
            boolean locked = false;
            try {
                locked = lock.obtain(lockWaitTimeout);
                return doBody();
            } finally {
                if (locked) {
                    lock.release();
                }
            }
        }
    }

}
