package ht.util.zookeeper;

import ht.util.core.HTException;
import ht.util.core.Log;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.leader.LeaderSelector;
import org.apache.curator.framework.recipes.leader.LeaderSelectorListener;
import org.apache.curator.framework.recipes.leader.LeaderSelectorListenerAdapter;
import org.apache.curator.framework.state.ConnectionState;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ZkLeadership {
    private static String basePath = "/leadership/";
    private ZKContext context;
    private HashMap<String, LeaderSelectorListener> listeners = new HashMap<>();
    private Map<String, LeaderSelector> selectors = new HashMap<>();
    private Map<String, LeaderCallback> callbacks = new HashMap<>();
    private Set<String> hasLeadership = new HashSet();

    public ZkLeadership(ZKContext context) {
        this.context = context;
    }

    public synchronized void setHaveLeadership(String name) {
        hasLeadership.add(name);
    }

    public synchronized void removeLeadership(String name) {
        hasLeadership.remove(name);
    }

    public synchronized boolean hasLeadership(String name) {
        return hasLeadership.contains(name);
    }

    public LeaderCallback relinquishLeadership(String name) {
        String path = basePath + name;
        if (this.selectors.containsKey(path)) {
            synchronized (this.selectors) {
                synchronized (this.listeners) {
                    LeaderSelector leaderSelector = this.selectors.get(path);
                    leaderSelector.close();
                    this.selectors.remove(path);
                    this.listeners.remove(path);
                    LeaderCallback remove = this.callbacks.remove(path);
                    return remove;
                }
            }
        }

        return null;
    }

    public void requestLeadership(final String name, final LeaderCallback callback) throws HTException {
        String path = basePath + name;
        Log.coordination.info("Requesting leadership %s", path);
        ZkLeadership me = this;
        LeaderSelectorListener lsl = new LeaderSelectorListenerAdapter() {

            @Override
            public void takeLeadership(CuratorFramework curatorFramework) {
                Log.coordination.info("Leading %s", path);
                me.setHaveLeadership(name);
                // callback is expected not to return control while it has leadership
                callback.takeLeadership(name);
                me.removeLeadership(name);
                Log.coordination.info("Not leading %s", path);
            }

            @Override
            public void stateChanged(CuratorFramework client, ConnectionState newState) {
                Log.coordination.info("State changed %s", newState.toString());
                super.stateChanged(client, newState);
            }
        };

        listeners.put(path, lsl);

        CuratorFramework curatorClient = context.getCuratorClient();

        LeaderSelector leaderSelector = new LeaderSelector(curatorClient, path, lsl);
        leaderSelector.autoRequeue();

        selectors.put(path, leaderSelector);
        callbacks.put(path, callback);

        leaderSelector.start();
    }
}
