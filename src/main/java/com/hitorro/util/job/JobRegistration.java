package com.hitorro.util.job;

import com.hitorro.util.core.classes.ClassUtil;

/**
 * Copyright (c) 2003 - present HiTorro All rights reserved. User: chris Date: Jan 25, 2005 Time: 1:17:26 PM
 */
public class JobRegistration {
    public String _name;
    public String _displayName;
    public String _viewName;
    public String _jobClassString;
    public Class _jobClass;
    public Class _parameterClass;

    public JobRegistration(Class jobClass, String displayName, Class parameterClass, String viewName) {
        _jobClass = jobClass;
        _displayName = displayName;
        _parameterClass = parameterClass;
        _viewName = viewName;
        Job aj = getAppJob();
        if (aj != null) {
            _name = aj.getName().toLowerCase();
        }
        if (jobClass != null) {
            _jobClassString = jobClass.getCanonicalName();
        }
    }

    public Job getAppJob() {
        return (Job) ClassUtil.getInstanceSwallowError(_jobClass, Job.class);
    }
}