package ht.util.testframework;

import ht.util.commandandcontrol.JUnitListener;
import ht.util.commandandcontrol.Response;
import ht.util.core.Env;
import ht.util.core.Log;
import ht.util.core.events.LocalEventHub;
import ht.util.core.string.Fmt;
import ht.util.log.Logger;
import ht.util.startupframework.ExitReasonObject;
import junit.framework.AssertionFailedError;
import junit.framework.Test;


/**
 * Copyright (c) 2003 - present HiTorro All rights reserved. User: chris
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
