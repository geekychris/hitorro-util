package com.hitorro.util.job;

import org.quartz.JobDataMap;

/**
 * <p/>
 * Copyright (c) 2003 - present HiTorro All rights reserved. User: chris Date: Sep 18, 2005 Time: 4:23:41 PM
 */
public class PropertiesJobParameters extends JobParameters {
    private JobDataMap dmap;

    public PropertiesJobParameters(JobDataMap dmap) {
        this.dmap = dmap;
    }

    public Object getProperty(String key) {
        return dmap.get(key);
    }

    public String getJobName() {
        return null;
    }

    public int getSerializationVersion() {
        return 0;
    }

    public boolean isPersisted() {
        return false;
    }

    public boolean hasGuid() {
        return false;
    }

    public boolean hasSoftGuid() {
        return false;
    }
}
