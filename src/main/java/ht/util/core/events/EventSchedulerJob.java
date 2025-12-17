package ht.util.core.events;

import ht.util.core.Log;
import ht.util.core.string.StringUtil;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;

/**
 * Copyright (c) 2003 - present HiTorro All rights reserved. User: chris Date: Jan 31, 2005 Time: 10:46:06 AM
 */
public class EventSchedulerJob implements Job {
    public EventSchedulerJob() {

    }

    public final void execute(JobExecutionContext jobExecutionContext) {
        JobDetail detail = jobExecutionContext.getJobDetail();
        JobDataMap dmap = detail.getJobDataMap();

        String event = (String) dmap.get("event");
        String sub = (String) dmap.get("subtopic");

        if (StringUtil.nullOrEmptyString(event)) {
            Log.util.error("No event name provided to the EventSchedulerJob");
            return;
        }
        LocalEventHub.get().event(event, sub, null);
    }

}
