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
package com.hitorro.util.core.thread.farm;

import com.hitorro.util.core.DeInitIntf;
import com.hitorro.util.core.Env;
import com.hitorro.util.core.iterator.queue.AbstractEnqueue;

/**
 * Farm that has a set of threads associated with it that pull work from a threaded queue of work objects. This work is
 * processed by the thread using an implementation provided by a FarmCommand. Once done, the output of the command is
 * placed on an output queue.
 *
 * @param <I> Input Queue Type
 * @param <O> OutputQueue Type
 * @param <T>
 * @author chris
 */
public class Farm<I, O, T> {
    private AbstractEnqueue<I> m_inputQueue;

    private AbstractEnqueue<O> m_outputQueue;

    private int m_threadCount = 4;

    private FarmThread<I, O, T> m_producers[];

    private boolean m_running = false;

    private long m_timeout = 10 * 1000;

    private FarmCommand<I, O, T> m_work;

    private String m_name = "<<NameNotSet>>";

    private ThreadGroup m_threadGroup;

    // if set to a positive value, we use a keep alive thread to ensure threads
    // are reborn if they
    // die
    private long millisDelay = -1;

    private Thread keepAliveThread = null;

    private Farm() {
        // do Nothing
    }

    /**
     * Construct a farm
     *
     * @param name        debug name of this farm.
     * @param group       threadgroup this guy will create threads in
     * @param in          threaded queue of elements of type I
     * @param out         threaded queue of elements O
     * @param threadCount amount of threads we will let rip against the queue
     */
    public Farm(String name, ThreadGroup group, AbstractEnqueue<I> in,
                AbstractEnqueue<O> out, FarmCommand<I, O, T> command, int threadCount) {
        m_threadGroup = group;
        setInputQueue(in);
        setOutputQueue(out);
        setName(name);
        setWorkerThreadCount(threadCount);
        m_work = command;
    }


    public Farm(ThreadGroup group) {
        m_threadGroup = group;
    }

    /**
     * Called once the farm is shut down.  Determines if all the farm threads have committed or rolled back
     * successfully.
     *
     * @return
     */
    public boolean waitForAllThreadsToComplete(int maxSeconds) {

        for (int tries = 0; tries < maxSeconds; tries++) {
            boolean success = true;
            for (int i = 0; i < m_producers.length; i++) {
                if (m_producers[i] != null) {
                    if (!m_producers[i].isCompleted()) {
                        success = false;
                        break;
                    }
                }
            }
            if (success) {
                return true;
            }
            Env.sleepNSeconds(1);
        }
        return false;
    }

    public void useKeepAliveThread(long millisDelay) {
        this.millisDelay = millisDelay;
    }

    /**
     * Start the pump pumping, until the we stop the queue.
     */
    public void start() {
        m_running = true;
        for (int i = 0; i < m_producers.length; i++) {
            m_producers[i].start();
        }
        if (millisDelay != -1) {
            FarmKeepAlive alive = new FarmKeepAlive(this, millisDelay);

            keepAliveThread = new Thread(m_threadGroup, alive, m_name);
            keepAliveThread.setDaemon(true);
            keepAliveThread.start();
        }
    }

    public boolean stop() {
        if (m_running == true) {
            for (int i = 0; i < m_producers.length; i++) {
                m_producers[i] = null;
            }
        }
        m_running = false;
        return true;
    }

    /**
     * Ensure that all threads that supposed to be running are running.
     *
     * @return
     */
    public boolean ensureAlive() {
        if (m_running == false) {
            // well just start, its not running.
            start();
            return true;
        }
        return keepAliveAux();
    }

    /**
     * get the display / debug name
     *
     * @return
     */
    public String getName() {
        return m_name;
    }

    /**
     * Set the display name (more for debug)
     *
     * @param name
     */
    public void setName(String name) {
        m_name = name;
    }

    public void setWorkerThreadCount(int count) {
        m_threadCount = count;
        initThreads();
    }

    public void setProducerWork(FarmCommand<I, O, T> work) {
        m_work = work;
    }

    public boolean running() {
        return m_running;
    }

    @SuppressWarnings("unchecked")
    public void initThreads() {
        m_running = true;
        m_producers = (FarmThread<I, O, T>[]) new FarmThread[m_threadCount];
        for (int i = 0; i < m_producers.length; i++) {
            m_producers[i] = new FarmThread(m_threadGroup, this, m_name, i);
        }
    }

    public FarmThread getThread(int i) {
        return m_producers[i];
    }

    public boolean hasItemsInPipeline() {
        return !(m_inputQueue.size() == 0 && m_outputQueue.size() == 0);
    }

    public AbstractEnqueue<I> getInputQueue() {
        return m_inputQueue;
    }

    /**
     * Set the input queue
     *
     * @param inputQueue
     */
    public void setInputQueue(AbstractEnqueue<I> inputQueue) {
        m_inputQueue = inputQueue;
    }

    public AbstractEnqueue<O> getOutputQueue() {
        return m_outputQueue;
    }

    /**
     * Set the output queue
     *
     * @param outputQueue
     */
    public void setOutputQueue(AbstractEnqueue<O> outputQueue) {
        m_outputQueue = outputQueue;
    }

    public long getTimeoutMillis() {
        return m_timeout;
    }

    /**
     * Called by the Farm Thread to carry out the work which in turn is deligate to the command work.
     *
     * @param work
     * @return
     */
    protected O produce(I work) {
        if (m_work != null) {
            return m_work.apply(work);
        }
        // that be bad...no worker
        return null;
    }

    /**
     * Once we have completed the queue we can tell each thread that we are complete.
     *
     * @return
     */
    protected void commit() {
        if (m_work != null) {
            m_work.commit();
        }
    }

    /**
     * Once we have canceled the queue we can tell each thread that we are rolling back.
     *
     * @return
     */

    protected void rollback() {
        if (m_work != null) {
            m_work.rollback();
        }
    }


    /**
     * util function to start the threads and to keep them alive.
     *
     * @return
     */
    @SuppressWarnings("unchecked")
    private boolean keepAliveAux() {
        for (int i = 0; i < m_producers.length; i++) {
            FarmThread p = m_producers[i];
            if (p == null || !p.isAlive()) {
                if (p != null) {
                    this.m_work.deinit(p);
                    if (p instanceof DeInitIntf) {
                        ((DeInitIntf) p).deinit();
                    }
                }
                m_producers[i] = new FarmThread(m_threadGroup, this, m_name, i);
                m_producers[i].start();
            }
        }
        return true;
    }

    /**
     * Worker thread has determined that the queue is complete and is exiting. Once all threads have exited we should
     * notify the output queue that it is complete.
     *
     * @return
     */
    synchronized void notifyThreadExitDueToQueueComplete() {
        m_threadCount--;
        if (m_threadCount == 0 && m_outputQueue != null) {
            // last one, tell the output queue that we have completed.
            m_outputQueue.setQueueComplete();
        }
    }

    class FarmKeepAlive implements Runnable {
        private Farm m_farm;

        private long m_sleepPeriod;

        public FarmKeepAlive(Farm farm, long sleepPeriod) {
            m_farm = farm;
            m_sleepPeriod = sleepPeriod;
        }

        public void run() {
            while (m_farm != null && m_farm.running()) {
                m_farm.ensureAlive();
                Env.sleepMillis(m_sleepPeriod);
            }
        }
    }
}
