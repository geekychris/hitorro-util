/*
 * Copyright (c) 2006-2025 Chris Collins
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.hitorro.util.core.thread;

import com.hitorro.util.startupframework.phases.ServiceDefinition;

import java.util.ArrayList;
import java.util.List;

/**
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
    private Thread serviceThread;


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
        serviceThread = new Thread(m_threadGroup, this, ServiceName);
        serviceThread.setDaemon(true);
        serviceThread.start();
        return null;
    }

    public String start(boolean dbInit) {
        return null;
    }

    public String deInit() {
        for (RestartableService service : m_services) {
            service.stop();
        }
        serviceThread = null;
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
