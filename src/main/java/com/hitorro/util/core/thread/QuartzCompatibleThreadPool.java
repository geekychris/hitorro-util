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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.SchedulerConfigException;
import org.quartz.spi.ThreadPool;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
/*
 * Copyright 2004-2005 OpenSymphony
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 */

/*
 * Previously Copyright (c) 2001-2004 James House
 */


/**
 * <p/> This is class is a simple implementation of a thread pool, based on the <code>{@link
 * org.quartz.spi.ThreadPool}</code> interface. </p> <p/> <p/> <CODE>Runnable</CODE> objects are sent to the pool with
 * the <code>{@link #runInThread(Runnable)}</code> method, which blocks until a <code>Thread</code> becomes available.
 * </p> <p/> <p/> The pool has a fixed number of <code>Thread</code>s, and does not grow or shrink based on demand.
 * </p>
 *
 * @author James House
 * @author Juergen Donnerstag
 */
public class QuartzCompatibleThreadPool implements ThreadPool {

    /*
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     *
     * Data members.
     *
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     */

    private final Object nextRunnableLock = new Object();
    private final Log log = LogFactory.getLog(getClass());
    private int count = -1;
    private int prio = Thread.NORM_PRIORITY;
    private boolean isShutdown = false;
    private boolean handoffPending = false;
    private boolean inheritLoader = false;
    private boolean inheritGroup = true;
    private boolean makeThreadsDaemons = false;
    private ThreadGroup threadGroup;
    private List workers;
    private LinkedList availWorkers = new LinkedList();
    private LinkedList busyWorkers = new LinkedList();
    private String threadNamePrefix = "SimpleThreadPoolWorker";
    private String schedInstId;
    private String schedName;

    /*
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     *
     * Constructors.
     *
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     */

    /**
     * <p/> Create a new (unconfigured) <code>SimpleThreadPool</code>. </p>
     *
     * @see #setThreadCount(int)
     * @see #setThreadPriority(int)
     */
    public QuartzCompatibleThreadPool() {
    }

    /**
     * <p/> Create a new <code>SimpleThreadPool</code> with the specified number of <code>Thread</code> s that have the
     * given priority. </p>
     *
     * @param threadCount    the number of worker <code>Threads</code> in the pool, must be > 0.
     * @param threadPriority the thread priority for the worker threads.
     * @see java.lang.Thread
     */
    public QuartzCompatibleThreadPool(int threadCount, int threadPriority) {
        setThreadCount(threadCount);
        setThreadPriority(threadPriority);
    }

    /*
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     *
     * Interface.
     *
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     */

    public Log getLog() {
        return log;
    }

    public int getPoolSize() {
        return getThreadCount();
    }


    public String getInstanceId() {
        return schedInstId;
    }

    //@Override
    public void setInstanceId(final String schedInstId) {
        this.schedInstId = schedInstId;
    }

    public String getInstanceName() {
        return schedName;
    }

    //@Override
    public void setInstanceName(final String schedName) {
        this.schedName = schedName;
    }

    /**
     * <p/> Get the number of worker threads in the pool. </p>
     */
    public int getThreadCount() {
        return count;
    }

    /**
     * <p/> Set the number of worker threads in the pool - has no effect after <code>initialize()</code> has been
     * called. </p>
     */
    public void setThreadCount(int count) {
        this.count = count;
    }

    /**
     * <p/> Get the thread priority of worker threads in the pool. </p>
     */
    public int getThreadPriority() {
        return prio;
    }

    /**
     * <p/> Set the thread priority of worker threads in the pool - has no effect after <code>initialize()</code> has
     * been called. </p>
     */
    public void setThreadPriority(int prio) {
        this.prio = prio;
    }

    public String getThreadNamePrefix() {
        return threadNamePrefix;
    }

    public void setThreadNamePrefix(String prfx) {
        this.threadNamePrefix = prfx;
    }

    /**
     * @return Returns the threadsInheritContextClassLoaderOfInitializingThread.
     */
    public boolean isThreadsInheritContextClassLoaderOfInitializingThread() {
        return inheritLoader;
    }

    /**
     * @param inheritLoader The threadsInheritContextClassLoaderOfInitializingThread to set.
     */
    public void setThreadsInheritContextClassLoaderOfInitializingThread(
            boolean inheritLoader) {
        this.inheritLoader = inheritLoader;
    }

    public boolean isThreadsInheritGroupOfInitializingThread() {
        return inheritGroup;
    }

    public void setThreadsInheritGroupOfInitializingThread(
            boolean inheritGroup) {
        this.inheritGroup = inheritGroup;
    }


    /**
     * @return Returns the value of makeThreadsDaemons.
     */
    public boolean isMakeThreadsDaemons() {
        return makeThreadsDaemons;
    }

    /**
     * @param makeThreadsDaemons The value of makeThreadsDaemons to set.
     */
    public void setMakeThreadsDaemons(boolean makeThreadsDaemons) {
        this.makeThreadsDaemons = makeThreadsDaemons;
    }

    public void initialize() throws SchedulerConfigException {

        if (count <= 0) {
            throw new SchedulerConfigException(
                    "Thread count must be > 0");
        }
        if (prio <= 0 || prio > 9) {
            throw new SchedulerConfigException(
                    "Thread priority must be > 0 and <= 9");
        }

        if (isThreadsInheritGroupOfInitializingThread()) {
            // follow the threadGroup tree to the root thread group.
            threadGroup = Thread.currentThread().getThreadGroup();
            ThreadGroup parent = threadGroup;
            while (!parent.getName().equals("main")) {
                threadGroup = parent;
                parent = threadGroup.getParent();
            }
            threadGroup = new EnhancedThreadGroup(parent, "HTSimpleThreadPool");
            if (isMakeThreadsDaemons()) {
                threadGroup.setDaemon(true);
            }
        }


        if (isThreadsInheritContextClassLoaderOfInitializingThread()) {
            getLog().info(
                    "Job execution threads will use class loader of thread: "
                            + Thread.currentThread().getName());
        }

        // create the worker threads and start them
        Iterator workerThreads = createWorkerThreads(count).iterator();
        while (workerThreads.hasNext()) {
            WorkerThread wt = (WorkerThread) workerThreads.next();
            wt.start();
            availWorkers.add(wt);
        }
    }

    protected List createWorkerThreads(int count) {
        workers = new LinkedList();
        for (int i = 1; i <= count; ++i) {
            WorkerThread wt = new WorkerThread(this, threadGroup,
                    getThreadNamePrefix() + "-" + i,
                    getThreadPriority(),
                    isMakeThreadsDaemons());
            if (isThreadsInheritContextClassLoaderOfInitializingThread()) {
                wt.setContextClassLoader(Thread.currentThread()
                        .getContextClassLoader());
            }
            workers.add(wt);
        }

        return workers;
    }

    /**
     * <p/> Terminate any worker threads in this thread group. </p> <p/> <p/> Jobs currently in progress will complete.
     * </p>
     */
    public void shutdown() {
        shutdown(true);
    }

    /**
     * <p/> Terminate any worker threads in this thread group. </p> <p/> <p/> Jobs currently in progress will complete.
     * </p>
     */
    public void shutdown(boolean waitForJobsToComplete) {

        synchronized (nextRunnableLock) {
            isShutdown = true;

            // signal each worker thread to shut down
            Iterator workerThreads = workers.iterator();
            while (workerThreads.hasNext()) {
                WorkerThread wt = (WorkerThread) workerThreads.next();
                wt.shutdown();
                availWorkers.remove(wt);
            }

            // Give waiting (wait(1000)) worker threads a chance to shut down.
            // Active worker threads will shut down after finishing their
            // current job.
            nextRunnableLock.notifyAll();

            if (waitForJobsToComplete == true) {

                // wait for hand-off in runInThread to complete...
                while (handoffPending) {
                    try {
                        nextRunnableLock.wait(100);
                    } catch (Throwable t) {
                    }
                }

                // Wait until all worker threads are shut down
                while (busyWorkers.size() > 0) {
                    WorkerThread wt = (WorkerThread) busyWorkers.getFirst();
                    try {
                        getLog().debug(
                                "Waiting for thread " + wt.getName()
                                        + " to shut down");

                        // note: with waiting infinite time the
                        // application may appear to 'hang'.
                        nextRunnableLock.wait(2000);
                    } catch (InterruptedException ex) {
                    }
                }

                int activeCount = threadGroup.activeCount();
                if (activeCount > 0) {
                    getLog().info(
                            "There are still " + activeCount + " worker threads active."
                                    + " See javadoc runInThread(Runnable) for a possible explanation");
                }

                getLog().debug("shutdown complete");
            }
        }
    }

    /**
     * <p/> Run the given <code>Runnable</code> object in the next isInitialized <code>Thread</code>. If while waiting
     * the thread pool is asked to shut down, the Runnable is executed immediately within a new additional thread. </p>
     *
     * @param runnable the <code>Runnable</code> to be added.
     */
    public boolean runInThread(Runnable runnable) {
        if (runnable == null) {
            return false;
        }

        synchronized (nextRunnableLock) {

            handoffPending = true;

            // Wait until a worker thread is isInitialized
            while ((availWorkers.size() < 1) && !isShutdown) {
                try {
                    nextRunnableLock.wait(500);
                } catch (InterruptedException ignore) {
                }
            }

            if (!isShutdown) {
                WorkerThread wt = (WorkerThread) availWorkers.removeFirst();
                busyWorkers.add(wt);
                wt.run(runnable);
            } else {
                // If the thread pool is going down, execute the Runnable
                // within a new additional worker thread (no thread from the pool).
                WorkerThread wt = new WorkerThread(this, threadGroup,
                        "WorkerThread-LastJob", prio, isMakeThreadsDaemons(), runnable);
                busyWorkers.add(wt);
                workers.add(wt);
                wt.start();
            }
            nextRunnableLock.notifyAll();
            handoffPending = false;
        }

        return true;
    }

    public int blockForAvailableThreads() {
        synchronized (nextRunnableLock) {

            while ((availWorkers.size() < 1 || handoffPending) && !isShutdown) {
                try {
                    nextRunnableLock.wait(500);
                } catch (InterruptedException ignore) {
                }
            }

            return availWorkers.size();
        }
    }

    protected void makeAvailable(WorkerThread wt) {
        synchronized (nextRunnableLock) {
            if (!isShutdown) {
                availWorkers.add(wt);
            }
            busyWorkers.remove(wt);
            nextRunnableLock.notifyAll();
        }
    }

    /*
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     *
     * WorkerThread Class.
     *
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     */

    /**
     * <p/> A Worker loops, waiting to execute tasks. </p>
     */
    class WorkerThread extends HTThread {

        // A flag that signals the WorkerThread to terminate.
        private boolean run = true;

        private QuartzCompatibleThreadPool tp;

        private Runnable runnable = null;

        /**
         * <p/> Create a worker thread and start it. Waiting for the next Runnable, executing it, and waiting for the
         * next Runnable, until the shutdown flag is set. </p>
         */
        WorkerThread(QuartzCompatibleThreadPool tp, ThreadGroup threadGroup, String name,
                     int prio, boolean isDaemon) {

            this(tp, threadGroup, name, prio, isDaemon, null);
        }

        /**
         * <p/> Create a worker thread, start it, execute the runnable and terminate the thread (one time execution).
         * </p>
         */
        WorkerThread(QuartzCompatibleThreadPool tp, ThreadGroup threadGroup, String name,
                     int prio, boolean isDaemon, Runnable runnable) {

            super(threadGroup, name);
            this.tp = tp;
            this.runnable = runnable;
            setPriority(prio);
            setDaemon(isDaemon);
        }

        /**
         * <p/> Signal the thread that it should terminate. </p>
         */
        void shutdown() {
            run = false;

            // Javadoc mentions that it interrupts blocked I/O operations as
            // well. Hence the job will most likely fail. I think we should
            // shut the work thread gracefully, by letting the job finish
            // uninterrupted. See SimpleThreadPool.shutdown()
            //interrupt();
        }

        public void run(Runnable newRunnable) {
            synchronized (this) {
                if (runnable != null) {
                    throw new IllegalStateException("Already running a Runnable!");
                }

                runnable = newRunnable;
                this.notifyAll();
            }
        }

        /**
         * <p/> Loop, executing targets as they are received. </p>
         */
        public void run() {
            boolean runOnce = (runnable != null);

            boolean ran = false;
            while (run) {
                try {
                    synchronized (this) {
                        while (runnable == null && run) {
                            this.wait(500);
                        }
                    }

                    if (runnable != null) {
                        ran = true;
                        runnable.run();
                    }
                } catch (InterruptedException unblock) {
                    // do nothing (loop will terminate if shutdown() was called
                    try {
                        getLog().error("worker threat got 'interrupt'ed.", unblock);
                    } catch (Exception e) {
                        // ignore to help with a tomcat glitch
                    }
                } catch (Exception exceptionInRunnable) {
                    try {
                        getLog().error("Error while executing the Runnable: ",
                                exceptionInRunnable);
                    } catch (Exception e) {
                        // ignore to help with a tomcat glitch
                    }
                } finally {
                    runnable = null;
                    // repair the thread in case the runnable mucked it up...
                    if (getPriority() != tp.getThreadPriority()) {
                        setPriority(tp.getThreadPriority());
                    }

                    if (runOnce) {
                        run = false;
                    } else if (ran) {
                        ran = false;
                        makeAvailable(this);
                    }

                }
            }

            //if (log.isDebugEnabled())
            try {
                getLog().debug("WorkerThread is shutting down");
            } catch (Exception e) {
                // ignore to help with a tomcat glitch
            }
        }
    }
}