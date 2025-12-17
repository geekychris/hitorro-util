package ht.util.core.thread;

/**
 * Copyright (c) HiTorro 2003-2008, Inc.
 * <p/>
 * User: chris Date: Jul 7, 2004 Time: 2:57:17 PM
 * <p/>
 * Description:
 */
public interface TimerCallback {
    /*
        callback from the timer.  If implementor returns true and the callback thread is
        in loop mode, the timer will wait again and re-invoke.
    */
    boolean callback();
}
