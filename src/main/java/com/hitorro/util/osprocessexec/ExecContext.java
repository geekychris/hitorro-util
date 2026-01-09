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
package com.hitorro.util.osprocessexec;

import com.hitorro.util.core.Log;
import com.hitorro.util.core.thread.EnhancedThreadFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 */
public class ExecContext {

    private static EnhancedThreadFactory m_factory = new EnhancedThreadFactory("ShellExecutor",
            "ShellExecutor", true);
    private static BlockingQueue<Runnable> m_workQueue = new PriorityBlockingQueue<Runnable>(20);

    private static ThreadPoolExecutor s_exec = new ThreadPoolExecutor(5, 20, 100, TimeUnit.SECONDS, m_workQueue, m_factory);
    WaitOnProcessRunner procThread = null;
    private boolean hasCompleted = false;
    private int m_exitCode = -1;
    private OutputStream m_outputStream = null;
    private OutputStream errorStream = null;
    private InputStream m_inputStream = null;
    private String m_program = null;
    private String[] m_args = null;
    private CopyRunner errorWriter = null;
    private CopyRunner outputWriter = null;
    private CopyRunner inputReader = null;
    private Process m_process = null;

    private Object m_lock = new Object();
    private TerminationKey terminationKey = null;

    public ExecContext(TerminationKey key) {
        terminationKey = key;
    }

    public Process getProcess() {
        return m_process;
    }

    public void setProgram(String program) {
        m_program = program;
    }

    public void setArgs(String args[]) {
        m_args = args;
    }

    public void setOutputFile(File osFile) throws FileNotFoundException {
        m_outputStream = new FileOutputStream(osFile);
    }

    public void setErrorFile(File errFile) throws FileNotFoundException {
        errorStream = new FileOutputStream(errFile);
    }

    public void setInputFile(File inputFile) throws FileNotFoundException {
        m_inputStream = new FileInputStream(inputFile);
    }

    public void setOutput(OutputStream os) {
        m_outputStream = os;
    }

    public void setError(OutputStream os) {
        errorStream = os;
    }

    public void setInput(InputStream is) {
        m_inputStream = is;
    }

    /**
     * Execute a process and then wait a number of miliseconds.  Following this we will terminate the process as its
     * taken too long.
     *
     * @param maxWaitTime
     * @return exit code of the launched command.
     * @throws java.io.IOException  if unable to launch program
     * @throws InterruptedException if timed out waiting.
     */
    public int execAndWait(long maxWaitTime)
            throws IOException, InterruptedException {
        //test();
        long startTime = System.currentTimeMillis();
        long endTime = startTime + maxWaitTime;
        try {

            if (m_args == null) {
                m_process = Runtime.getRuntime().exec(m_program);
            } else {
                m_process = Runtime.getRuntime().exec(m_program, m_args);
            }

            createIOThreads();
            if (m_process != null) {
                // should set input stream and output stream!!!
                procThread = new WaitOnProcessRunner(this);
                s_exec.execute(procThread);
                while (hasCompleted != true) {
                    // determine if we timed out.
                    long currTime = System.currentTimeMillis();
                    if (currTime > endTime) {
                        // out of time, destroy the process and then throw an exception;
                        // Question is, what happens to the input output and error stream?
                        m_process.destroy();

                        throw new InterruptedException("Tired of waiting for command to complete");
                    }

                    // wait a little
                    try {
                        synchronized (m_lock) {
                            m_lock.wait(1000);
                        }
                    } catch (InterruptedException e) {
                        // do nothing
                    }
                }
                Log.util.debug("execAndWait succeeded ");
                return m_exitCode;
            } else {
                Log.util.debug("Process was null in ExecContext %s", m_program);
            }

            Log.util.debug("Shouldnt really get here as we assume we always have a process object.");
            return -1;
        } finally {
            ensureThreadsStopped();
        }
    }

    public int execAndWaitFromProcessBuilder(long maxWaitTime)
            throws IOException, InterruptedException {
        //test();
        long startTime = System.currentTimeMillis();
        long endTime = startTime + maxWaitTime;
        try {
            List<String> args = new ArrayList<String>();
            ProcessBuilder pb;
            if (m_args != null) {
                args.add(m_program);
                for (String arg : m_args) {
                    args.add(arg);
                }
                pb = new ProcessBuilder(args);
            } else {
                pb = new ProcessBuilder(m_program);
            }
            Map<String, String> env = pb.environment();
            //pb.directory("myDir");

            m_process = pb.start();

            createIOThreads();
            if (m_process != null) {
                // should set input stream and output stream!!!
                procThread = new WaitOnProcessRunner(this);
                s_exec.execute(procThread);
                while (hasCompleted != true) {
                    // determine if we timed out.
                    long currTime = System.currentTimeMillis();
                    if (currTime > endTime) {
                        // out of time, destroy the process and then throw an exception;
                        // Question is, what happens to the input output and error stream?
                        m_process.destroy();

                        throw new InterruptedException("Tired of waiting for command to complete");
                    }

                    // wait a little
                    try {
                        synchronized (m_lock) {
                            m_lock.wait(1000);
                        }
                    } catch (InterruptedException e) {
                        // do nothing
                    }
                }
                Log.util.debug("execAndWait succeeded ");
                return m_exitCode;
            } else {
                Log.util.debug("Process was null in ExecContext %s", m_program);
            }

            Log.util.debug("Shouldnt really get here as we assume we always have a process object.");
            return -1;
        } finally {
            ensureThreadsStopped();
        }
    }

    /**
     * Startup a process and then
     *
     * @return
     * @throws IOException
     */
    public boolean exec()
            throws IOException {
        m_process = Runtime.getRuntime().exec(m_program, m_args);
        createIOThreads();
        if (m_process != null) {
            // should set input stream and output stream!!!
            procThread = new WaitOnProcessRunner(this);
            s_exec.execute(procThread);
            return true;
        } else {
            Log.util.debug("Process was null in ExecContext %s", m_program);
            return false;
        }
    }

    public boolean completed() {
        return hasCompleted;
    }

    public int getExitCode() {
        return this.m_exitCode;
    }

    public void destroy() {
        m_process.destroy();
    }

    private void createIOThreads()
            throws IOException {

        // processes input is my output
        if (m_outputStream != null) {
            outputWriter = createOutputThread(m_process.getInputStream(), m_outputStream);
        }

        //process error output is my error input
        if (errorStream != null) {
            errorWriter = createOutputThread(m_process.getErrorStream(), errorStream);
        }

        //process output is my input
        if (m_inputStream != null) {
            inputReader = createInputThread(m_process.getOutputStream(), m_inputStream);
        }
    }

    private void ensureThreadsStopped() {
        if (m_process != null) {
            m_process.destroy();
            // ensure we wait for the thing to die
            try {


                m_process.waitFor();
            } catch (InterruptedException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
        attemptToStopThread(outputWriter);
        attemptToStopThread(errorWriter);
        attemptToStopThread(inputReader);
        attemptToStopThread(procThread);
    }

    private void attemptToStopThread(Runnable t) {
        if (t != null) {
            s_exec.remove(t);
        }
    }

    private CopyRunner createOutputThread(InputStream is, OutputStream out) {

        if (is == null) {
            return null;
        }

        /*
        if (outputFile == null) {
            return null;
        }
        */

        CopyRunner t = new CopyRunner();
        t.writeStreamFromInputStream(is, out);
        s_exec.execute(t);

        return t;
    }

    private CopyRunner createInputThread(java.io.OutputStream os, InputStream is) {
        if (os == null) {
            return null;
        }

        /*
        if (inputFile == null)
        {
            return null;
        }
        */

        CopyRunner t = new CopyRunner();
        t.readStreamIntoStream(os, is);
        s_exec.execute(t);
        return t;
    }

    public void notifyComplete(int exitCode) {
        m_exitCode = exitCode;
        hasCompleted = true;
        synchronized (m_lock) {
            Log.util.debug("Notifying ExecContext");
            m_lock.notify();
            if (terminationKey != null) {
                terminationKey.complete(m_exitCode);
            }
            Log.util.debug("Notified ExecContext");
        }
    }
}


