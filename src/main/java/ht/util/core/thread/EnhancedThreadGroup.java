package ht.util.core.thread;

import ht.util.core.Log;

/**
 * Extension of thread group for logging unhandled exceptions
 *
 * @author chris
 */
public class EnhancedThreadGroup extends ThreadGroup {
    public EnhancedThreadGroup(String name) {
        super(name);
    }

    public EnhancedThreadGroup(ThreadGroup parent, String name) {
        super(parent, name);
    }

    /**
     * Ensure we log
     */
    @Override
    public void uncaughtException(Thread t, Throwable e) {
        Log.util.error("Exception not caught %s %e", e, e);
        if (e instanceof OutOfMemoryError) {
            // we dont stay around anymore
            // note that fatal normally exits.
            Log.util.fatal("Shutting down system because of non recoverable error", e);
        }
    }
}
