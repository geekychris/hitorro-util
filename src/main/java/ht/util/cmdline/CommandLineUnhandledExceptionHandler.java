package ht.util.cmdline;

import ht.util.core.Log;

/**
 * Report any exceptions not caught by the thread of execution.
 */
public class CommandLineUnhandledExceptionHandler implements Thread.UncaughtExceptionHandler {
    public void uncaughtException(Thread thread, Throwable e) {
        Log.util.error("Thread %s had an unhandled exception %s %e", thread.toString(), e, e);
    }
}
