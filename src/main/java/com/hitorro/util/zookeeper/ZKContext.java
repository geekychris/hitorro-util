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
package com.hitorro.util.zookeeper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.hitorro.jsontypesystem.JVS;
import com.hitorro.util.commandandcontrol.ano.ArgType;
import com.hitorro.util.commandandcontrol.ano.CommandDef;
import com.hitorro.util.commandandcontrol.ano.DebugArgAno;
import com.hitorro.util.core.ArrayUtil;
import com.hitorro.util.core.Constants;
import com.hitorro.util.core.HTException;
import com.hitorro.util.core.iterator.AbstractIterator;
import com.hitorro.util.core.iterator.json.BaseJsonIterator;
import com.hitorro.util.io.StringInputStream;
import com.hitorro.util.json.keys.*;
import com.hitorro.util.startupframework.phases.ServiceDefinition;
import org.apache.curator.CuratorZookeeperClient;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryOneTime;
import org.apache.curator.utils.EnsurePath;
import org.apache.curator.utils.ZKPaths;
import org.apache.curator.x.async.AsyncCuratorFramework;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


@ServiceDefinition(
        shortName = "zookeeper",
        description = "zookeeper",
        debugCommands = {},
        typeManagedClasses = {},
        uiDirectories = {})
public class ZKContext {
    public static final com.hitorro.util.json.keys.IntegerProperty ExhibitorPollingMillis = new com.hitorro.util.json.keys.IntegerProperty("zookeeper.exhibitor.pollingmillis", "", (int) (60 * Constants.MillisInSecond));
    public static final com.hitorro.util.json.keys.IntegerProperty SleepMillisBetweenRetries = new com.hitorro.util.json.keys.IntegerProperty("zookeeper.sleepmillisbtwretries", "", (int) (5 * Constants.MillisInSecond));
    public static final com.hitorro.util.json.keys.IntegerProperty MaxSleepMillisBetweenRetries = new com.hitorro.util.json.keys.IntegerProperty("zookeeper.maxsleepmillisbtwretries", "", (int) (50 * Constants.MillisInSecond));
    public static final com.hitorro.util.json.keys.IntegerProperty MaxRetries = new com.hitorro.util.json.keys.IntegerProperty("zookeeper.maxretries", "", 29);
    public static final com.hitorro.util.json.keys.BooleanProperty ZookeeperInProcess = new com.hitorro.util.json.keys.BooleanProperty("zookeeper.inprocess", "", false);
    public static final com.hitorro.util.json.keys.BooleanProperty ZookeeperConnectByIP = new com.hitorro.util.json.keys.BooleanProperty("zookeeper.connectbyip", "", true);
    public static final String HOSTINFO_NAME = "hostinfo.name";
    public static final String HOSTINFO_IP = "hostinfo.ip";
    public static final String ExhibitorClusterListEndpoint = "/exhibitor/v1/cluster/list";
    public static com.hitorro.util.json.keys.StringProperty zkHost = new com.hitorro.util.json.keys.StringProperty("zookeeper.host", "", "localhost");
    public static com.hitorro.util.json.keys.LongProperty zkPort = new com.hitorro.util.json.keys.LongProperty("zookeeper.port", "", 2181l);

    //@Dependency(name = "ClusterManager")
    //public ClusterManager cm;
    public static ZKContext me;
    public static com.hitorro.util.json.keys.StringProperty pathKey = new com.hitorro.util.json.keys.StringProperty("path", "", null);
    public static com.hitorro.util.json.keys.StringProperty jobKey = new com.hitorro.util.json.keys.StringProperty("job", "", null);
    public static com.hitorro.util.json.keys.StringProperty colectionKey = new com.hitorro.util.json.keys.StringProperty("collection", "", null);
    public static com.hitorro.util.json.keys.StringProperty capabilityKey = new com.hitorro.util.json.keys.StringProperty("capability", "", null);
    public static com.hitorro.util.json.keys.StringListFromDelimitedKey paramNameKey = new com.hitorro.util.json.keys.StringListFromDelimitedKey("paramname", "", ",", null);
    public static com.hitorro.util.json.keys.StringListFromDelimitedKey paramValueKey = new com.hitorro.util.json.keys.StringListFromDelimitedKey("paramvalue", "", ",", null);
    private CuratorFramework client;
    private AsyncCuratorFramework asyncClient;
    private boolean zkinprocess;
    private boolean _shutdown = false;
    private String referencePath;
    private ZkLeadership leadership = new ZkLeadership(this);

    public ZkLeadership getLeadershipContext() {
        return leadership;
    }

    public void init(boolean dbInit, final boolean upgrading, final long currentVersion, final long targetVersion) {
        me = this;
        zkinprocess = ZookeeperInProcess.apply();
        referencePath = ZookeeperConnectByIP.apply() ? HOSTINFO_IP : HOSTINFO_NAME;
    }

    public String start(boolean dbInit) {
        return null;
    }

    public void deInit() {

        try {
            getCuratorClient().close();
        } catch (HTException e) {
            e.printStackTrace();
        }
        client = null;
        _shutdown = true;
    }

    public boolean exists(String path) throws HTException {
        try {
            CuratorZookeeperClient zookeeperClient = getCuratorClient().getZookeeperClient();
            ZooKeeper zooKeeper = zookeeperClient.getZooKeeper();

            Stat stat = zooKeeper.exists(path, false);

            return stat != null;
        } catch (Exception e) {
            throw new HTException("%s %e", e, e);
        }
    }

    public void create(String path, byte[] bytes) throws HTException {
        try {
            ZooKeeper zooKeeper = getCuratorClient().getZookeeperClient().getZooKeeper();
            ZKPaths.mkdirs(zooKeeper, path);

            Stat stat = new Stat();
            zooKeeper.getData(path, false, stat);
            zooKeeper.setData(path, bytes, stat.getVersion());
        } catch (Exception e) {
            throw new HTException("%s %e", e, e);
        }
    }

    public void create(String path, JsonNode contents) throws HTException {
        try {
            ZooKeeper zooKeeper = getCuratorClient().getZookeeperClient().getZooKeeper();
            ZKPaths.mkdirs(zooKeeper, path);

            Stat stat = new Stat();
            zooKeeper.getData(path, false, stat);
            zooKeeper.setData(path, contents.toString().getBytes(), stat.getVersion());
        } catch (Exception e) {
            throw new HTException("%s %e", e, e);
        }
    }

    private ZooKeeper getZK() throws Exception {
        return getCuratorClient().getZookeeperClient().getZooKeeper();
    }

    public List<String> listChildren(String path) throws HTException {
        try {
            return getZK().getChildren(path, false);

        } catch (Exception e) {
            throw new HTException("%s %e", e, e);
        }
    }

    public JsonNode getContentsAsJson(String path) throws HTException {
        try {
            byte[] data = getRawContents(path);

            AbstractIterator<JsonNode> map = new BaseJsonIterator(new StringInputStream(data));
            return map.getFirstItemAndClose();
        } catch (Exception e) {
            throw new HTException("%s %e", e, e);
        }
    }

    public byte[] getRawContents(String path) throws HTException {
        try {
            CuratorZookeeperClient zookeeperClient = getCuratorClient().getZookeeperClient();

            Stat stat = new Stat();
            new EnsurePath(path).ensure(zookeeperClient);
            return getZK().getData(path, false, stat);
        } catch (Exception e) {
            throw new HTException("%s %e", e, e);
        }
    }

    public void delete(String path) throws HTException {
        try {
            Stat stat = new Stat();
            getZK().delete(path, stat.getVersion());
        } catch (Exception e) {
            throw new HTException("%s %e", e, e);
        }
    }

    public void watch(String path, final WatchCallback callback) throws HTException {
        try {
            Watcher watcher = new Watcher() {
                WatchCallback cback = callback;

                @Override
                public void process(WatchedEvent event) {
                    WatchCallback.WatchCallbackType t = WatchCallback.WatchCallbackType.None;
                    switch (event.getType()) {
                        case None:
                            t = WatchCallback.WatchCallbackType.None;
                            break;
                        case NodeCreated:
                            t = WatchCallback.WatchCallbackType.Created;
                            break;
                        case NodeDeleted:
                            t = WatchCallback.WatchCallbackType.Deleted;
                            break;
                        case NodeDataChanged:
                            t = WatchCallback.WatchCallbackType.DataChanged;
                            break;
                        case NodeChildrenChanged:
                            t = WatchCallback.WatchCallbackType.ChildrenChanged;
                            break;
                    }
                    callback.processEvent(t);
                }
            };

            getZK().getChildren(path, watcher);

        } catch (Exception e) {
            throw new HTException("%s %e", e, e);
        }

    }

    @CommandDef(command = "zk.deletepath", description = "", isInternal = true)
    public boolean deletePath(@DebugArgAno(keyName = "",
            description = "",
            defaultValue = "",
            argType = ArgType.Raw) String raw,
                              @DebugArgAno(keyName = "",
                                      description = "",
                                      defaultValue = "",
                                      argType = ArgType.Args) JVS args) throws HTException {
        String path = new com.hitorro.util.json.keys.StringProperty("", "path", null).apply(args);
        delete(path);
        return true;
    }

    @CommandDef(command = "zk.listchildren", description = "", isInternal = true)
    public JsonNode getChildren(@DebugArgAno(keyName = "",
            description = "",
            defaultValue = "",
            argType = ArgType.Raw) String raw,
                                @DebugArgAno(keyName = "",
                                        description = "",
                                        defaultValue = "",
                                        argType = ArgType.Args) JVS args) throws HTException {
        ArrayNode jsonNodes = JsonNodeFactory.instance.arrayNode();

        String path = new com.hitorro.util.json.keys.StringProperty("", "path", null).apply(args);

        List<String> contents = listChildren(path);
        for (Iterator<String> iterator = contents.iterator(); iterator.hasNext(); ) {
            String next = iterator.next();
            jsonNodes.add(next);
        }
        return jsonNodes;
    }

    @CommandDef(command = "zk.path", description = "", isInternal = true)
    public JsonNode getPath(@DebugArgAno(keyName = "",
            description = "",
            defaultValue = "",
            argType = ArgType.Raw) String raw,
                            @DebugArgAno(keyName = "",
                                    description = "",
                                    defaultValue = "",
                                    argType = ArgType.Args) JVS args) {
        String path = new com.hitorro.util.json.keys.StringProperty("", "path", null).apply(args);

        return getContentsAsJson(path);
    }

    @CommandDef(command = "zk.createpath", description = "", isInternal = true)
    public synchronized JsonNode createPath(@DebugArgAno(keyName = "",
            description = "",
            defaultValue = "",
            argType = ArgType.Raw) String raw,
                                            @DebugArgAno(keyName = "",
                                                    description = "",
                                                    defaultValue = "",
                                                    argType = ArgType.Args) JVS args) throws HTException {
        String path = pathKey.apply(args);
        String job = jobKey.apply(args);
        String collection = colectionKey.apply(args);
        String capability = capabilityKey.apply(args);
        String[] params = paramNameKey.getArray(args);
        String[] values = paramValueKey.getArray(args);


        ObjectNode jsonNodes = JsonNodeFactory.instance.objectNode();

        jsonNodes.put("job", job);
        jsonNodes.put("collection", collection);
        jsonNodes.put("capability", capability);

        if (!ArrayUtil.nullOrEmpty(params)) {
            for (int i = 0; i < params.length; i++) {
                String param = params[i];
                String value = values[i];

                jsonNodes.put(param, value);
            }
        }

        create(path, jsonNodes);

        return jsonNodes;
    }

    public String getConnectString() {
        String hostname = zkHost.apply();
        Long port = zkPort.apply();
        return hostname + ":" + port;
    }

    public synchronized CuratorFramework getCuratorClient() {
        if (client == null) {
            long timingSession = Constants.MillisInSecond * 10;
            client = CuratorFrameworkFactory.newClient(getConnectString(), (int) timingSession, (int) timingSession, new RetryOneTime(1));
            client.start();
        }
        return client;
    }

    public synchronized AsyncCuratorFramework getCuratorAsyncClient() {
        if (asyncClient == null) {
            asyncClient = AsyncCuratorFramework.wrap(getCuratorClient());
        }
        return asyncClient;
    }

    public synchronized CuratorFramework getCuratorClientExhibitor() throws HTException {
       /* if (client == null && !_shutdown)
        {

            RetryPolicy retryPolicy = new ExponentialBackoffRetry(SleepMillisBetweenRetries.apply(),
                                                                  MaxRetries.apply(),
                                                                  MaxSleepMillisBetweenRetries.apply());

            Collection<String> hostnames = getZKHosts();

            if (hostnames.size() == 0)
            {
                return null;
            }

            int restPort = exhibitorServicePort();

            Log.coordination.info("Zookeeper Hosts %s", hostnames);

            Exhibitors exhibitors = new Exhibitors(hostnames, restPort, new Exhibitors.BackupConnectionStringProvider()
            {
                @Override
                public String getBackupConnectionString ()
                {
                    return "";
                }
            });

            ExhibitorEnsembleProvider ensembleProvider = new ExhibitorEnsembleProvider(exhibitors,
                                                                                       new DefaultExhibitorRestClient(),
                                                                                       ExhibitorClusterListEndpoint,
                                                                                       ExhibitorPollingMillis.apply(),
                                                                                       retryPolicy);

            client = CuratorFrameworkFactory.builder().ensembleProvider(ensembleProvider).retryPolicy
                    (retryPolicy).build();

            client.start();
        }

        return client;*/
        return null;
    }

    /**
     * XXX replace with host discovery / params
     *
     * @return
     * @throws HTException
     */
    private List<String> getZKHosts() throws HTException {
        try {
            ArrayList<String> hosts = new ArrayList<>();

            hosts.add("localhost");
            return hosts;
        } catch (Exception e) {
            throw new HTException("%s %e", e, e);
        }
    }

    private int exhibitorServicePort() {
        return 2181;
    }

    public interface WatchCallback {
        void processEvent(WatchCallbackType eventType);

        enum WatchCallbackType {
            None, Created, Deleted, Changed, DataChanged, ChildrenChanged
        }
    }
}