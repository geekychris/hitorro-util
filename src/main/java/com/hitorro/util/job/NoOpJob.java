package com.hitorro.util.job;

import org.apache.log4j.Level;

/**
 * Copyright (c) 2003 - present HiTorro All rights reserved.
 * <p/>
 * User: chris Given a jobparameters, simply say its done.
 */
public class NoOpJob extends Job {
    public static final String Name = "NoOpJob";

    public NoOpJob() {

    }

    public String getName() {
        return Name;
    }

    public boolean needsSession() {
        return true;
    }

    public JobExecutionResult doAction(JobParameters parameters) {
        return new JobExecutionResult(Level.INFO, "Done");
    }
}
