package ht.util.scheduler;

/**
 * <p/>
 * Copyright (c) 2003 - present HiTorro All rights reserved. User: chris Date: Feb 4, 2008 Time: 9:31:10 AM
 */
public class SchedulerContext {
    private static SchedulerIntf sched;

    public static void set(SchedulerIntf s) {
        sched = s;
    }

    public static SchedulerIntf get() {
        return sched;
    }
}
