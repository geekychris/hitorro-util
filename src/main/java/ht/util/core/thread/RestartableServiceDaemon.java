package ht.util.core.thread;

import ht.util.startupframework.phases.ServiceDefinition;

import java.util.ArrayList;
import java.util.List;

/**
 * Copyright (c) 2003 - present HiTorro All rights reserved. User: chris Date: Dec 9, 2006 Time: 9:25:59 PM
 */
@ServiceDefinition(dependentService = {},
        shortName = "restartable",
        description = "Restartable service",
        debugCommands = {},
        typeManagedClasses = {},
        uiDirectories = {},
        dependentServiceInterfaces = {})
public class RestartableServiceDaemon implements Runnable {
    private static final String ServiceName = "RestartableServiceDaemon";
    // 5 second sleep
    private static final long SleepTime = 1000 * 5;
    private static final Object s_lock = new Object();
    private static List<RestartableService> m_services = new ArrayList<RestartableService>();
    private EnhancedThreadGroup m_threadGroup = new EnhancedThreadGroup(ServiceName);
    private Thread m_serviceThread;


    public static void addService(RestartableService service) {
        synchronized (s_lock) {
            m_services.add(service);
            service.startService(true);
        }

    }

    public List<String> getDependentModules() {
        // no dependencies
        return null;
    }

    public String init(boolean dbInit, final boolean upgrading, final long currentVersion, final long targetVersion) {
        m_serviceThread = new Thread(m_threadGroup, this, ServiceName);
        m_serviceThread.setDaemon(true);
        m_serviceThread.start();
        return null;
    }

    public String start(boolean dbInit) {
        return null;
    }

    public String deInit() {
        for (RestartableService service : m_services) {
            service.stop();
        }
        m_serviceThread = null;
        return null;
    }

    public void run() {
        while (true) {
            synchronized (s_lock) {
                for (RestartableService service : m_services) {
                    service.ensureAlive();
                }
            }
            try {
                Thread.sleep(SleepTime);
            } catch (InterruptedException e) {
            }
        }
    }
}
