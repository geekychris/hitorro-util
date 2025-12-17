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
package com.hitorro.util.core;

import com.hitorro.util.core.string.Fmt;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.remote.JMXServiceURL;
import java.lang.management.ManagementFactory;
import java.net.MalformedURLException;

/**
 *
 */
public class JMXUtil {
    private static final String FMTString = "service:jmx:rmi:///jndi/rmi://%s:/jmxrmi";

    /**
     * @param managedObject
     * @param realm
     * @param type
     * @param name
     */
    public static void registerForJMX(Object managedObject, String realm, String type, String name) {
        String m = Fmt.S("%s:type=%s,name=%s", realm, type, name);
        registerForJMX(managedObject, m);
    }

    public static JMXServiceURL getJMXUrl(String host, int port) throws MalformedURLException {
        String serviceUrl = Fmt.S(FMTString, host, port);
        return new JMXServiceURL(serviceUrl);
    }

    private static void registerForJMX(Object jmxObject, String name) {
        MBeanServer beanServer = ManagementFactory.getPlatformMBeanServer();
        try {
            ObjectName on = new ObjectName(name);
            if (!beanServer.isRegistered(on)) {
                synchronized (beanServer) {
                    if (!beanServer.isRegistered(on)) {
                        Log.jmx.debug("Registering jmx: %s", name);
                        beanServer.registerMBean(jmxObject, on);

                    }
                }
            }
        } catch (Exception e) {
            Log.jmx.error("Failed to register %s %s %e %s", jmxObject, name, e, e);
        }

    }


}
