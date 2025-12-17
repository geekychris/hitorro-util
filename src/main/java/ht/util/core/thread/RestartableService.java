package ht.util.core.thread;

import ht.util.core.Log;

import java.util.HashMap;

/**
 *
 */
public class RestartableService {
    private static HashMap<String, EnhancedThreadGroup> s_groups = new HashMap<String, EnhancedThreadGroup>();
    private int m_restartCount;
    private String m_name;
    private String m_groupName;
    private EnhancedThreadGroup m_threadGroup;
    private Runnable m_runner;
    private Thread m_thread;
    private boolean m_shutdownServer;
    private boolean m_check = true;
    private RestartableRunnableIntf intf;
    private boolean closed = false;

    private RestartableService() {

    }

    public RestartableService(String name,
                              String groupName,
                              int restartCount,
                              Runnable runner,
                              boolean shutdownServer) {
        m_name = name;
        m_groupName = groupName;
        m_threadGroup = s_groups.get(groupName);
        if (m_threadGroup == null) {
            m_threadGroup = new EnhancedThreadGroup(groupName);
            s_groups.put(groupName, m_threadGroup);
        }
        m_restartCount = restartCount;
        m_runner = runner;
        m_shutdownServer = shutdownServer;
        if (runner instanceof RestartableRunnableIntf) {
            intf = (RestartableRunnableIntf) runner;
        }
    }

    /**
     * Stop the service, we are shutting down
     */
    @SuppressWarnings("deprecation")
    public void stop() {
        m_check = false;
        m_thread = null;
    }

    /**
     * As long as we havent start out of retries we will continue restarting the service.
     */
    public void ensureAlive() {
        if (m_check == true) {
            if (m_restartCount > 0) {
                startService(false);
            } else {
                if (m_shutdownServer == true) {
                    Log.util.fatal("Shutting down system because service %s could no longer be restarted", m_name);
                }
            }
        }
    }

    public void startService(boolean firstTime) {
        if ((m_thread == null || !m_thread.isAlive()) && !closed) {
            if (intf != null) {
                if (intf.isFinished()) {
                    intf.closeRestartable();
                    closed = true;
                    return;
                }
            }

            m_thread = new Thread(m_threadGroup, m_runner, m_name);
            m_thread.setDaemon(true);
            m_thread.start();
            if (firstTime == false) {
                Log.util.error("Restarting service %s, changes remaining %s", m_name, m_restartCount);
            }
            m_restartCount--;
        }


    }

    /**
     * Name of service.
     *
     * @return
     */
    public String getServiceName() {
        return m_name;
    }

    /**
     * Name of the threadgroup that running thread will be assigned to
     *
     * @return name
     */
    public String getServiceThreadGroupName() {
        return m_groupName;
    }
}




