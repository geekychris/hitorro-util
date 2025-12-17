package ht.util.core;

import ht.util.core.string.Fmt;

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
