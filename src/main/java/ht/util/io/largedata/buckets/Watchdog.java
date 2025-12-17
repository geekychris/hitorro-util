package ht.util.io.largedata.buckets;

import ht.util.core.Log;
import ht.util.core.thread.TimerCallback;

import java.io.IOException;

/**
 *
 */
public class Watchdog implements TimerCallback {
    private MaturingBucketWriter mbw;

    public Watchdog(MaturingBucketWriter p) {
        mbw = p;
    }

    /*
     * callback from the timer. If implementor returns true and the callback
     * thread is in loop mode, the timer will wait again and re-invoke.
     */
    public boolean callback() {
        try {
            mbw.flushBucketIfMature();
        } catch (IOException e) {
            Log.util.error("Unable to flush bucket %s %e", e, e);
        }
        return true;
    }
}