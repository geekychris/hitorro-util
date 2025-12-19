package com.hitorro.util.job;

import com.hitorro.util.cluster.ClusterServiceInterface;
import com.hitorro.util.core.string.StringUtil;
import com.hitorro.util.startupframework.ServiceContext;
import com.hitorro.util.typesystem.BaseSession;
import org.quartz.JobExecutionException;

import java.io.IOException;

/**
 * Copyright (c) 2003 - present HiTorro All rights reserved. User: chris Date: Jan 25, 2005 Time: 6:02:21 PM
 */
public abstract class Job {
    // our database session
    private BaseSession _session;


    public boolean canRunOnThisNode() {
        String name = getMustRunOnlyOnNamedSingleton();
        boolean runOnDB = getMustRunOnDBLeader();
        ClusterServiceInterface csi = (ClusterServiceInterface) ServiceContext.getSC().getServiceInterface(ClusterServiceInterface.class);
        if (csi != null) {
            // we have a cluster tier running so we must consider singletons etc
            if (!StringUtil.nullOrEmptyString(name)) {
                return csi.getAmINamedSingleton(name);
            }
            if (runOnDB) {
                return csi.canRunIfDBLeader();
            }
        }
        return true;

    }

    /**
     * Some jobs must only be running on a single node within the cluster.  In that case we ensure that if you must run
     * on that node and your NOT that node you will not run.
     *
     * @return
     */
    public String getMustRunOnlyOnNamedSingleton() {
        return null;
    }

    /**
     * Nodes can all be pointing to one database.  In that model, we cannot have multiple jobs running on different
     * nodes.
     *
     * @return
     */
    public boolean getMustRunOnDBLeader() {
        return false;
    }

    /**
     * Name that the job is known as, so that you do not need to refer to it by package.class name
     *
     * @return
     */
    public abstract String getName();

    public abstract boolean needsSession();

    /**
     * Get the database session for the job.
     *
     * @return the session to use when running this job.
     */
    public final BaseSession getSession() {
        return _session;
    }

    public void setSession(BaseSession session) {
        _session = session;
    }

    /**
     * Do the work of the job. This method is passed both a parameters object and a jobData object.  The parameters
     * object will be non-null in the case of a job run from a database-persisted instance.  The jobData will be
     * non-null if the job is being run from a configuration.  The parameters object should have priority.
     *
     * @param parameters - an instance of this job's parameter object.
     * @throws JobExecutionException on error
     */
    public abstract JobExecutionResult doAction(JobParameters parameters)
            throws JobExecutionException, IOException;

}

