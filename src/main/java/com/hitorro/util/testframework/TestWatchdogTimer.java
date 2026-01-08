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
package com.hitorro.util.testframework;

import com.hitorro.util.commandandcontrol.JUnitListener;
import com.hitorro.util.commandandcontrol.Response;
import com.hitorro.util.core.Env;
import com.hitorro.util.core.Log;
import com.hitorro.util.core.events.LocalEventHub;
import com.hitorro.util.core.string.Fmt;
import com.hitorro.util.log.Logger;
import com.hitorro.util.startupframework.ExitReasonObject;
import junit.framework.AssertionFailedError;
import junit.framework.Test;


/**
 */
public class TestWatchdogTimer implements Runnable {
    private int timeoutSeconds;
    private int counter = 0;
    private int secondsSleep = 5;
    private boolean running = true;
    private Thread myThread;
    private String name;
    private Response response;
    private Test test;
    private JUnitListener listener;

    public TestWatchdogTimer() {
    }

    public void setTimeout(int timeoutSeconds) {
        this.timeoutSeconds = timeoutSeconds;
    }

    public void setResponse(Response resp, JUnitListener listener) {
        response = resp;
        this.listener = listener;
    }

    public void prolongTest(int seconds) {
        this.name = name;
        this.test = test;
        counter = 0;
        timeoutSeconds = seconds;
    }

    public void reset(String name, Test test) {
        this.name = name;
        this.test = test;
        counter = 0;
    }

    public void start() {
        running = true;
        myThread = new Thread(this);
        myThread.start();
    }

    public void run() {
        while (running) {
            Env.sleepNSeconds(secondsSleep);
            counter += secondsSleep;
            if (counter > timeoutSeconds) {
                ExitReasonObject reasonObject = new ExitReasonObject(Fmt.S("TestServer Timed out waiting for %s test to complete.", name), -1);
                AssertionFailedError afe = new AssertionFailedError("Timed out");
                listener.addFailure(test, afe);
                listener.endTest(test);
                response.end();
                LocalEventHub.get().event(Logger.ExitEvent, "", reasonObject);
            }
            Log.test.info("Watchdog waiting for: %s", name);
        }
    }

    public void stop() {
        running = false;
    }
}
