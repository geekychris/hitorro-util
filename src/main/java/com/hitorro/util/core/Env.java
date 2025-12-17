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

import com.fasterxml.jackson.databind.JsonNode;
import com.sun.management.UnixOperatingSystemMXBean;
import com.hitorro.jsontypesystem.JVS;
import com.hitorro.jsontypesystem.propreaders.JVSProperties;
import com.hitorro.util.basefile.fs.BaseFile;
import com.hitorro.util.basefile.fs.file.FileFileSystem;
import com.hitorro.util.cmdline.BaseCommandLine;
import com.hitorro.util.commandandcontrol.CommandSession;
import com.hitorro.util.commandandcontrol.RedirectHttp;
import com.hitorro.util.commandandcontrol.ano.ArgType;
import com.hitorro.util.commandandcontrol.ano.CommandDef;
import com.hitorro.util.commandandcontrol.ano.DebugArgAno;
import com.hitorro.util.commandandcontrol.responsemappings.MapMapping;
import com.hitorro.util.core.iterator.AbstractIterator;
import com.hitorro.util.core.iterator.ArrayIterator;
import com.hitorro.util.core.map.MapUtil;
import com.hitorro.util.core.params.HTProperties;
import com.hitorro.util.core.string.Fmt;
import com.hitorro.util.core.string.StringUtil;
import com.hitorro.util.html.*;
import com.hitorro.util.io.FileUtil;
import com.hitorro.util.json.keys.BooleanProperty;
import com.hitorro.util.json.keys.IntegerProperty;
import com.hitorro.util.json.keys.StringProperty;
import com.hitorro.util.json.keys.propaccess.PropaccessError;
import com.hitorro.util.log.Logger;
import org.w3c.dom.Document;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.lang.management.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.*;
import java.util.*;

enum RedirectEnum {
    gms("gms", "google mail search",
            "https://mail.google.com/mail/u/0/#search/%s",
            "https://mail.google.com/mail/"),

    fb("fb", "fb [insert query]' searching on facebook. defaults on fb homepage",
            "http://www.facebook.com/s.php?q=%s&init=q",
            "http://www.facebook.com"),

    g("g", "g [insert query]' searching google",
            "http://www.google.com/search?q=%s",
            "http://www.google.com"),


    tiny("tiny", "tiny [insert query] Uses tinyurl to generate the shortened url",
            "http://tinyurl.com/api-create.php?url=%s",
            "http://tinyurl.com"),

    gm("gm", "gm [insert number from 0-n where n = number of gmail accounts - 1] opens up gmail",
            "https://mail.google.com/mail/u/%s/#inbox",
            "https://mail.google.com/mail/u/0/#inbox"),

    cpp("cpp", "cpp [insert query] searches for syntactical cpp terms on cppreference.com",
            "http://en.cppreference.com/mwiki/index.php?title=Special%3ASearch&search=%s",
            "http://en.cppreference.com/w/"),

    yt("yt", "yt [insert query] make a youtube search. If not query is passed in, defaults to the youtube homepage",
            "http://www.youtube.com/results?search_query=%s&search_type=&aq=-1&oq=",
            "http://www.youtube.com"),

    d("d", "d [insert query] make a google definition search. If not query is passed in, defaults to dictionary.com",
            "https://www.google.com/search?q=define+%s",
            "http://www.dictionary.com/");


    public static EnumContext<RedirectEnum> redirectContext;
    protected String description;
    protected String arg;
    protected String noArg;

    RedirectEnum(String shortName, String description, String arg, String noArg) {
        this.description = description;
        this.arg = arg;
        this.noArg = noArg;
        setMapEntry(this, shortName);
    }

    private static void setMapEntry(RedirectEnum filter, String shortName) {
        if (redirectContext == null) {
            redirectContext = new EnumContext("redirects");
        }
        redirectContext.setNames(filter, shortName);
    }

    public String getWithArg(String q) {
        return Fmt.S(arg, q);
    }

    public String getWithoutArg() {
        return noArg;
    }
}

/**
 * Environment related functions.
 *
 * @author chris
 */
public class Env {
    public static final StringProperty ServerType = new StringProperty("servertype", "type of server it has one", null);
    public static final String HT_HOME = "ht_home";
    public static final String HT_BIN = "ht_bin";
    public static final String HT_DATA = "ht_data";
    public static final String START_TIME = "starttime";
    public static final String START_TIME_MILLIS = "starttime";
    public static final String HOST_IP = "hostip";
    public static final String HOST_NAME = "hostname";
    // On unix this is empty, on windows this is .exe
    public static final String EXEC_EXTENSION = "exec_extension";
    // this is a subdirectory under HT_BIN/script/<PLAT>
    public static final String SCRIPT_PLAT_DIR = "script_plat_dir";
    // on unix this is /usr/bin
    public static final String OS_BIN_DIR = "os_bin_dir";
    public static final BooleanProperty DebugMode = new BooleanProperty("jni.debugbuildlocation", "If debug mode we look first in the original build path", false);
    //Identifier of the server, this server should have a unique id over the complete network, it should be made up of the
    //servertype and node number
    public static final StringProperty ServerId = new StringProperty("serverid", "Id of the server instance including server type", "<<UndefinedServerId>>");
    public static final String PRIVATE = "private";
    public static final IntegerProperty HttpPort = new IntegerProperty("defaulthttp.port", "number for http port", 8072);
    /**
     * Set the following in the environment to overide where Binary and home are located....this is to seperate out
     * configuration of a system vs the binary distribution directories.
     */
    private static final String HiTorroBin = "HT_BIN";
    private static final String HiTorroHome = "HT_HOME";
    private static final StringProperty DatabaseId =
            new StringProperty("databaseid",
                    "Id of the database instance, if not private then it is assumed it is shared and has to be coordinated", PRIVATE);
    private static final StringProperty NodeComment =
            new StringProperty("nodecomment",
                    "Comment about node that can be seen in the instance definition", "");
    private static final StringProperty RealmId =
            new StringProperty("realmid",
                    "realm in which this node falls.  Used to segment a cluster by sub clusters", "default");
    // GlobalId, represents a persisted source (db), many server instances may share this global id but servers not
    // sharing the same database should not share the global id
    private static final IntegerProperty GlobalId = new IntegerProperty("globalid", "The global id of an underlying db instance", 1);
    private static final IntegerProperty NodeIdentity = new IntegerProperty("nodeid", "Id of the server instance", 0);
    private static final IntegerProperty NodeId = new IntegerProperty("nodeid", "Id of the server instance", 0);

    private static Platform s_platform = null;
    private static File s_openResourceDir;
    private static Integer s_globalId = null;
    private static Integer s_nodeId = null;
    private static String s_serverId = null;
    private static String s_databaseId = null;
    private static String s_nodeComment = null;
    private static String s_realmId = null;
    private static String machineKey = null;
    private static FileFileSystem binFileSystem = null;
    private static FileFileSystem homeFileSystem = null;
    private static File s_scriptDir = null;


    /**
     * Dumb routine
     *
     * @param value
     * @return
     */
    public static long getNextPower2Value(long start, long value) {
        while (start < value) {
            start = start * 2;
        }
        return start;
    }

    /**
     * general data about machine to put in a props file that could be used as a build tag
     *
     * @param props
     */
    public static final void addMachineInfoToProps(HTProperties props) {
        props.put("date", new Date().toString());
        props.put("host_name", Env.getHostName());

        props.put("server_type", Env.getServerType());

        props.put("server_id", Env.getServerId());

        props.put("ip_address", Env.getHostIP());
    }

    public static File getSavedProps() {
        return getSavedProps(null);
    }

    public static File getSavedProps(JsonNode propsSoFar) {
        return new File(Env.getHome(), Fmt.S("%s-saved.properties", Env.NodeId.apply(propsSoFar)));
    }

    public static File getSavedJVSProps(JVS propsSoFar) {
        return new File(Env.getHome(), Fmt.S("%s-saved.json", Env.NodeIdentity.apply(propsSoFar)));
    }

    public static final BaseFile getBaseFile(File file) {
        return FileFileSystem.Root.getFile(file.getAbsolutePath());
    }

    public static int getHTTPPort() {
        return HttpPort.apply();
    }

    public static final boolean loadLib(String lib, String pathIn) {
        try {
            // loadLibrary seems to require no path (must be in LD_LIBRARY_PATH
            //System.loadLibrary("/ht/repos/platform/src/ht/jni/hitorroJNI");
            String path = Env.getNativeLib(pathIn, lib);
            System.load(path);
            //System.loadLibrary(path);
            return true;
        } catch (UnsatisfiedLinkError e) {
            Log.util.error("Cannot load jni library. %s %e", e, e);
        }
        return false;
    }

    /**
     * This should be defined as a parameter to be set!!! XXX TODO
     *
     * @return
     */
    public static final String getSystemLanguage() {
        return "en_us";
    }

    @CommandDef(command = "env.id.createuniqueid", description = "return a system unique id, never to be repeated again", isInternal = false)
    public static String getUniqueId() {
        long time = System.currentTimeMillis();
        String name = Env.getServerId();
        return Fmt.S("%s.%s", Long.toString(time), name);
    }

    @CommandDef(command = "env.dir.uiresource", description = "ui resource directory", isInternal = false)
    public static final String getUIResourceDir() {
        String resource = null;
        try {
            resource = JVSProperties.getProperties().getString("ht_resource");
        } catch (PropaccessError propaccessError) {
            return null;
        }

        if (StringUtil.nullOrEmptyString(resource)) {
            return new File(Env.getBin(), "build").getAbsolutePath();
        }
        return resource;
    }

    @CommandDef(command = "env.dir.configdir", description = "config resource directory", isInternal = false)
    public static final String getConfigDir() {
        return new File(Env.getBin(), "config").getAbsolutePath();
    }

    public static final BaseFile getBinConfigBaseFile() {
        return getBinBaseFileSystem().getFile("config");
    }

    public static final BaseFile getHomeConfigBaseFile() {
        return getHomeBaseFileSystem().getFile("config");
    }

    public static final String getNativeLib(String fullPath, String lib) {
        boolean debug = DebugMode.apply();
        if (debug) {
            return Fmt.S("%s/src/%s/%s.%s", Env.getBin(), fullPath, lib, Platform.getPlatform().getSharedObjectExtension());
        }
        return Fmt.S("%s/build/native/%s.%s", Env.getBin(), lib, Platform.getPlatform().getSharedObjectExtension());
    }

    public static final boolean isUnixVariant() {
        return Platform.getPlatform().isUnixVariant();
    }

    /**
     * path seperators are used for constructing classpaths and "PATH" variables.
     *
     * @return
     */
    public static final String getPathSeperator() {
        return File.pathSeparator;
    }

    /**
     * Provide a reason for exiting
     *
     * @param reason
     * @param exitCode
     */
    public static final void exitSystem(String reason, int exitCode) {
        String s = Fmt.S("%s with exit code %s", reason, exitCode);
        if (exitCode < 0) {
            Console.eprintln(s);
        }

        Log.util.info(s);
        System.exit(exitCode);
    }

    public static final File getArchiveLogDir() {
        return Logger.s_archiveDir;
    }

    public static final String getServerType() {
        return ServerType.apply();
    }

    public static final String getDatabaseId() {
        if (s_databaseId == null) {
            s_databaseId = DatabaseId.apply();
        }
        return s_databaseId;
    }

    public static final String getNodeComment() {
        if (s_nodeComment == null) {
            s_nodeComment = NodeComment.apply();
        }
        return s_nodeComment;
    }

    public static final String getRealmId() {
        if (s_realmId == null) {
            s_realmId = RealmId.apply();
        }
        return s_realmId;
    }

    /**
     * Global id of this instance.
     *
     * @return
     */
    @CommandDef(command = "env.id.global", description = "global id of the system", isInternal = false)
    public static final Integer getGlobalId() {
        if (s_globalId == null) {
            s_globalId = GlobalId.apply();
        }
        return s_globalId;
    }

    @CommandDef(command = "env.id.server", description = "server id of the system", isInternal = false)
    public static final String getServerId() {
        if (s_serverId == null) {
            s_serverId = ServerId.apply();
        }
        return s_serverId;
    }

    /**
     * Key that defines a unique VM
     *
     * @return
     */
    @CommandDef(command = "env.machinekey", description = "Unique key that makes up this VM", isInternal = false)
    public static String getMachineKey() {
        if (machineKey == null) {
            String tmp = Fmt.S("%s-%s-%s", Env.getHostIP(), Env.getServerId(), Env.getGlobalId());
            machineKey = tmp;
        }
        return machineKey;
    }

    @CommandDef(command = "env.id.node", description = "node id of the system", isInternal = false)
    public static final Integer getNodeId() {
        if (s_nodeId == null) {
            s_nodeId = NodeId.apply();
        }
        return s_nodeId;
    }

    @CommandDef(command = "env.dir.log", description = "log directory of the system", isInternal = false)
    public static final File getLogDir() {
        return new File(getHome(), "logs");
    }

    public static final File getCurrentLogDir() {
        return new File(getLogDir(), "current");
    }

    public static final File getTestLogDir() {
        return new File(getLogDir(), "tests");
    }

    @CommandDef(command = "env.dir.openresource", description = "open resource directory", isInternal = false)
    public static final File getOpenResourceDir() {
        if (s_openResourceDir == null) {
            s_openResourceDir = new File(getHome(), "openresource");
            FileUtil.ensureDirectoryExists(s_openResourceDir);
        }
        return s_openResourceDir;
    }

    public static File getSolrConfigRootFile() {
        return new File(getResource(), "solr-webapp");
    }

    public static final File getBin() {
        // supposedly getenv is depre
        String bin = System.getProperty(HiTorroBin);
        if (StringUtil.nullOrEmptyString(bin)) {
            bin = System.getenv(HiTorroBin);
        }

        if (StringUtil.nullOrEmptyString(bin)) {
            return determineBINFromClasspath();
        }
        return new File(bin);
    }

    public static FileFileSystem getBinBaseFileSystem() {
        if (binFileSystem == null) {
            binFileSystem = new FileFileSystem(getBin());
        }
        return binFileSystem;
    }

    public static BaseFile getBinBaseFile() {

        return getBinBaseFileSystem().getFile("");
    }

    public static final File getData() {
        return new File(getBin(), "data");
    }

    public static final File getResource() {
        return new File(getBin(), "resources");
    }

    public static final BaseFile getDataBaseFile() {
        return getBinBaseFileSystem().getFile("data");
    }

    public static BaseFile getWordnetDirectory() {
        return Env.getDataBaseFile().getChild("WordNet-3.0");
    }

    /**
     * Temp Directory.
     *
     * @return
     */
    public static final File getTempDirectory() {
        return new File(System.getProperty("java.io.tmpdir"));
    }

    public static final File getDTDDirectory() {
        return new File(getBin(), "/data/dtds");
    }

    /**
     * The story of HT_HOME: Normally one should set the HT_HOME in the os environment.  This is so that it can be read
     * early on in the instantiation of the process.  home is used to determine such things as HT_HOME/config, so of
     * course we cant read HT_HOME from the config.
     * <p/>
     * For reasons of dev work, we now have a way to set this via VM args.  This is so you can set home from idea or
     * your ide of choice.  Do it as follows:
     * <p/>
     * -DHT_HOME=/path.
     * <p/>
     * Uses HiTorro_BIN if all else fails.
     *
     * @return
     */
    public static final File getHome() {
        String bin = System.getProperty("HT_HOME");
        if (StringUtil.nullOrEmptyString(bin)) {
            bin = System.getenv(HiTorroHome);
        }
        // supposedly getenv is deprecated

        if (StringUtil.nullOrEmptyString(bin)) {
            return getBin();
        }
        return new File(bin.trim());
    }

    public static FileFileSystem getHomeBaseFileSystem() {
        if (homeFileSystem == null) {
            homeFileSystem = new FileFileSystem(getHome());
        }
        return homeFileSystem;
    }

    public static BaseFile getHomeBaseFile() {
        return getHomeBaseFileSystem().getFile("");
    }

    public static final File determineBINFromClasspath() {
        URL u = ClassLoader.getSystemResource("config.txt");
        if (u == null) {
            return null;
        }
        String propsDirFile = u.toExternalForm();
        File f = null;
        try {
            URL url = new URL(propsDirFile);
            f = new File(url.getFile());
        } catch (MalformedURLException e) {
            return null;
        }
        if (StringUtil.nullOrEmptyString(propsDirFile)) {
            return null;
        }
        if (f == null) {
            return null;
        }
        f = f.getParentFile().getParentFile();
        return f;

    }

    /**
     * Get the name of the host.
     *
     * @return hostname
     */
    public static final String getHostName() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            return null;
        }
    }

    @CommandDef(command = "env.hostip", description = "host ip address", isInternal = false)
    public static final String getHostIP() {
        try {
            InetAddress[] addresses = InetAddress.getAllByName(getHostName());
            if (!ArrayUtil.nullOrEmpty(addresses)) {
                return addresses[0].getHostAddress();
            }
            return InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            Log.util.error("Unable to get hostIP %s %e", e, e);
            return null;
        }
    }

    /**
     * This function doesnt seem to work very well for the purpose of getting a blind IP address there can be ipv6 and
     * docker ip addresses when one just onces a meaningfull ipv4 adderss to ping this host.
     *
     * @return
     */
    public static final String getHostIPFromNetworkInterfaces() {
        try {
            Enumeration e = NetworkInterface.getNetworkInterfaces();
            List<String> hosts = new ArrayList();
            while (e.hasMoreElements()) {
                NetworkInterface n = (NetworkInterface) e.nextElement();
                Enumeration ee = n.getInetAddresses();
                while (ee.hasMoreElements()) {
                    InetAddress i = (InetAddress) ee.nextElement();
                    hosts.add(i.getHostAddress());
                }
            }
            if (hosts.size() > 1) {
                return hosts.get(1);
            }
            return hosts.get(0);
        } catch (SocketException e) {
            Log.util.error("Unable to get hostIP %s %e", e, e);
            return null;
        }
    }

    /**
     * Get a dump of all the properties.
     */
    public static final String getPrettyPrintedJavaProperties() {
        Properties props = System.getProperties();
        return getPrettyPrintedProperties(props);
    }

    public static final String getPrettyPrintedProperties(Map props) {
        StringBuilder buff = new StringBuilder();
        Iterator iter = MapUtil.keyIterator(props);
        buff.append("Process java properties: \n");
        while (iter.hasNext()) {
            String key = (String) iter.next();
            buff.append(key);
            buff.append(" = ");
            buff.append(props.get(key));
            buff.append(Constants.NewLineChar);
        }
        return buff.toString();
    }

    /**
     * Make the thread wait n seconds.
     *
     * @param seconds
     * @return true if waited, false if interrupted.
     */
    public static final boolean sleepNSeconds(int seconds) {
        try {
            Thread.sleep(seconds * Constants.MillisInSecond);
            return true;
        } catch (InterruptedException e) {
            return false;
        }
    }

    /**
     * Wait on an object getting notified, or a timeout occuring.
     *
     * @param seconds
     * @param lock
     * @return
     */
    public static final boolean sleepNSeconds(int seconds, Object lock) {
        synchronized (lock) {
            try {
                lock.wait(seconds * Constants.MillisInSecond);
                return true;
            } catch (InterruptedException e) {
                return false;
            }
        }
    }

    /**
     * Make the thread wait n seconds.
     *
     * @param millis
     * @return true if waited, false if interrupted.
     */
    public static final boolean sleepMillis(long millis) {
        try {
            Thread.sleep(millis);
            return true;
        } catch (InterruptedException e) {
            return false;
        }
    }

    /**
     * Get the stack trace of the caller and return it as a String
     *
     * @return
     */
    public static final String getCurrentStackTrace() {
        try {
            throw new Exception("ExceptionUsedForStackTraceOnly");
        } catch (Exception ex) {
            StackTraceElement[] stacks = ex.getStackTrace();
            StringBuilder buf = new StringBuilder();
            // ignore top frame - that's us
            for (int i = 1; i < stacks.length; i++) {
                buf.append(stacks[i].toString()).append("\n");
            }
            return buf.toString();
        }
    }

    @CommandDef(command = "env.dir.scripts", description = "script directory", isInternal = false)
    public static final File getPlatformScriptDirectory() {
        if (s_scriptDir == null) {
            s_scriptDir = new File(HTProperties.getProperties().get(SCRIPT_PLAT_DIR));
        }
        return s_scriptDir;
    }

    // On unix this is empty, on windows this is .exe
    //public static final String EXEC_EXTENSION = "EXEC_EXTENSION";
    // this is a subdirectory under HT_BIN/script/<PLAT>
    //public static final String SCRIPT_PLAT_DIR = "SCRIPT_PLAT_DIR";
    // on unix this is /usr/bin
    //public static final String OS_BIN_DIR = "OS_BIN_DIR";

    /**
     * Set of hard coded configuration parameters
     *
     * @return
     */
    @CommandDef(command = "env.systemargs", description = "system arguments", isInternal = false, resultMapper = MapMapping.class)
    public static final Map<String, String> getSystemArgs() {
        Map<String, String> map = new TreeMap<String, String>();
        map.put(HT_HOME, Env.getHome().getAbsolutePath());
        map.put(HT_BIN, Env.getBin().getAbsolutePath());
        map.put(HT_DATA, Fmt.S("%s/data", Env.getBin().getAbsolutePath()));
        map.put(START_TIME, new Date().toString());
        map.put(START_TIME_MILLIS, Long.toString(new Date().getTime()));
        if (StringUtil.nullOrEmptyString(Env.getHostIP())) {
            map.put(HOST_IP, "localhost");
        } else {
            map.put(HOST_IP, Env.getHostIP());
        }
        map.put(HOST_NAME, Env.getHostName());

        Platform.getPlatform().addParamsToEnvironment(map);

        return map;
    }

    public static final void addVMPropsToMap(JVS map) throws PropaccessError {
        map.set("os.VmVersion", getJavaVmVersion());
        map.set("os.RuntimeVersion", getJavaRuntimeVersion());
        map.set("os.OsArchitecture", getOsArch());
        map.set("os.OsServicePack", getOsServicePack());
        map.set("os.VmVendor", getVmVendor());

        map.set("os.OsName", getOsName());
        map.set("os.Compiler", getManagementCompiler());
        map.set("os.OsVersion", getOsVersion());

    }

    private static final void addRow(Map<String, Object> map,
                                     String name,
                                     Object value) {
        String val;
        if (value == null) {
            value = "";
        }
        map.put(name, value);
    }

    public static void getJavaVMInfo(Map<String, Object> map) {
        addRow(map, "java.vm.version", System.getProperty("java.vm.version"));
        addRow(map, "java.vm.vendor", System.getProperty("java.vm.vendor"));
        addRow(map, "java.vm.vendor", System.getProperty("java.vm.vendor"));
        addRow(map, "java.runtime.version", System.getProperty("java.runtime.version"));
        addRow(map, "java.arch.data.model", System.getProperty("java.arch.data.model"));
        addRow(map, "os.version", System.getProperty("os.version"));
        addRow(map, "sun.os.patch.level", System.getProperty("sun.os.patch.level"));
    }

    public static void getBasicProcessInfo(Map<String, Object> map, ThreadMXBean threadMXBean) {
        addRow(map, "start", Fmt.formatDateTime(BaseCommandLine.getCommandLine().processStartTime));
        addRow(map, "delta", Fmt.formatDateTimeDelta(System.currentTimeMillis(), BaseCommandLine.getCommandLine().processStartTime));
        addRow(map, "threadcount", Integer.toString(threadMXBean.getThreadCount()));
        addRow(map, "availprocessors", Integer.toString(Runtime.getRuntime().availableProcessors()));
        addRow(map, "Runtime.FreeMemory", Long.toString(Runtime.getRuntime().freeMemory()));
        addRow(map, "Runtime.TotalMemory", Long.toString(Runtime.getRuntime().totalMemory()));
        addRow(map, "command", BaseCommandLine.getCommandLine().commandLine);
    }

    public static void getHeapMemoryInfo(Map<String, Object> map) {
        MemoryMXBean bean = ManagementFactory.getMemoryMXBean();
        int count = bean.getObjectPendingFinalizationCount();

        MemoryUsage mu = bean.getHeapMemoryUsage();
        addRow(map, "HeapMemoryUsage.ObjectsPendingFinalization", Long.toString(count));
        addRow(map, "HeapMemoryUsage.Commited", StringUtil.getBytesNeatForm(mu.getCommitted()));

        addRow(map, "HeapMemoryUsage.Init", StringUtil.getBytesNeatForm(mu.getInit()));
        addRow(map, "HeapMemoryUsage.Max", StringUtil.getBytesNeatForm(mu.getMax()));
        addRow(map, "HeapMemoryUsage.Used", StringUtil.getBytesNeatForm(mu.getUsed()));
        MemoryUsage nonHeap = bean.getNonHeapMemoryUsage();
        addRow(map, "NonHeapMemoryUsage.Commited", StringUtil.getBytesNeatForm(nonHeap.getCommitted()));

        addRow(map, "NonHeapMemoryUsage.Init", StringUtil.getBytesNeatForm(nonHeap.getInit()));
        addRow(map, "NonHeapMemoryUsage.Max", StringUtil.getBytesNeatForm(nonHeap.getMax()));
        addRow(map, "NonHeapMemoryUsage.Used", StringUtil.getBytesNeatForm(nonHeap.getUsed()));
    }

    public static void getOSInfo(Map<String, Object> map) {
        OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
        if (osBean instanceof UnixOperatingSystemMXBean) {
            UnixOperatingSystemMXBean uosBean = (UnixOperatingSystemMXBean) osBean;
            addRow(map, "build.openfiledescriptors", uosBean.getOpenFileDescriptorCount());
            addRow(map, "build.maxfiledescriptors", uosBean.getMaxFileDescriptorCount());
            addRow(map, "build.totalswap", uosBean.getTotalSwapSpaceSize());
            addRow(map, "build.processscputime", uosBean.getProcessCpuTime());


            addRow(map, "build.commitedvirtual", uosBean.getCommittedVirtualMemorySize());
            addRow(map, "build.freephysicalmemory", uosBean.getFreePhysicalMemorySize());
            addRow(map, "build.freeswapspace", uosBean.getFreeSwapSpaceSize());
            addRow(map, "build.availProcessors", uosBean.getAvailableProcessors());

            if (Platform.getPlatform().isUnixVariant()) {
                // HACK because java 1.5 on a mac does not have this method.
                Class c = uosBean.getClass();

                try {
                    Method m = c.getMethod("getSystemLoadAverage");
                    Object arguments[] = {};
                    Object o = m.invoke(uosBean, arguments);
                    addRow(map, "os.loadAverage", o);

                } catch (NoSuchMethodException e) {

                } catch (InvocationTargetException e) {

                } catch (IllegalAccessException e) {

                }
            }
        }
        addRow(map, "os.Architecture", osBean.getArch());
        addRow(map, "os.name", osBean.getName());
        addRow(map, "os.version", osBean.getVersion());
    }

    public static void getGCInfo(Map<String, Object> map) {
        List<GarbageCollectorMXBean> gcMXBeans = ManagementFactory.getGarbageCollectorMXBeans();
        if (gcMXBeans != null && gcMXBeans.size() > 0) {

            for (GarbageCollectorMXBean gcMXBean : gcMXBeans) {
                if (gcMXBean.getCollectionCount() > 0) {
                    addRow(map, "gc.mode", gcMXBean.getName());
                    addRow(map, "g.ccollections", Long.toString(gcMXBean.getCollectionCount()));
                    addRow(map, "gc.approximatTimeSeconds", Long.toString(gcMXBean.getCollectionTime() / 1000));
                }
            }
        }

    }

    public static final String getJavaVmVersion() {
        return System.getProperty("java.vm.version");
    }

    public static final String getJavaRuntimeVersion() {
        return System.getProperty("java.runtime.version");
    }

    public static final String getOsArch() {
        return System.getProperty("os.arch");
    }

    public static final String getOsServicePack() {
        return System.getProperty("sun.os.patch.level");
    }

    public static final String getVmVendor() {
        return System.getProperty("java.vm.vendor");
    }

    public static final String getOsName() {
        return System.getProperty("os.name");
    }

    public static final String getManagementCompiler() {
        return System.getProperty("sun.management.compiler");
    }

    public static final String getOsVersion() {
        return System.getProperty("os.version");
    }

    @CommandDef(command = "env.time", description = "Get the current system time")
    public static Date dumpTime() {
        return new Date();
    }

    @CommandDef(command = "dump.classpath", description = "fuel price")
    public static void dumpClassPath() {
        String c = System.getProperty("java.class.path");

        AbstractIterator<String> iter = new ArrayIterator<String>(StringUtil.tokenizeFromSingleChar(c, ":"));
        while (iter.hasNext()) {
            Console.println("%s", iter.next());
        }
    }

    @CommandDef(command = "f", description = "fuel price")
    public static String fuel() throws IOException {
        String url = "https://www.gasbuddy.com/station/14394";
        com.hitorro.util.html.HTMLPageFetcher fetcher = new com.hitorro.util.html.HTMLPageFetcher();
        com.hitorro.util.html.HTMLPage page = fetcher.fetchPage(url);
        com.hitorro.util.html.HTMLParser parser = page.getParser();
        parser.setFilters(new String[]{"SCRIPT"});
        Document doc = parser.getDocument();
        com.hitorro.util.html.DumpTree dt = new com.hitorro.util.html.DumpTree();
        dt.setDOMParser(parser.getDomParser());
        com.hitorro.util.html.DumpTreeCollector dtc = new com.hitorro.util.html.DumpTreeCollector();
        dt.visit("SCRIPT", new URL(url), dtc);
        return "";
    }

    @CommandDef(command = "r", description = "redirect")
    public static RedirectHttp redirect(@DebugArgAno(keyName = "",
            description = "",
            defaultValue = "",
            argType = ArgType.Raw) String raw,
                                        @DebugArgAno(keyName = "",
                                                description = "",
                                                defaultValue = "",
                                                argType = ArgType.Args) JVS args,
                                        @DebugArgAno(keyName = "",
                                                description = "",
                                                defaultValue = "",
                                                argType = ArgType.Uri) String uri,
                                        @DebugArgAno(keyName = "",
                                                description = "",
                                                defaultValue = "",
                                                argType = ArgType.Request) HttpServletRequest req
    ) {
        RedirectHttp http = new RedirectHttp();
        String lineRaw = null;
        try {
            lineRaw = args.getString("q");
        } catch (PropaccessError propaccessError) {
            return null;
        }
        String parts[] = StringUtil.splitByToken(lineRaw, ' ');
        if (parts.length >= 1) {
            RedirectEnum r = RedirectEnum.redirectContext.getByShortName(parts[0]);
            if (r == null) {
                r = RedirectEnum.g;
            }
            if (parts.length == 1) {
                http.setPath(r.getWithoutArg());
            } else {
                http.setPath(r.getWithArg(parts[1]));
            }
        }
        return http;
    }


    @CommandDef(command = "env.javaprops", description = "Dump the java properties", resultMapper = MapMapping.class)
    public static Map<Object, Object> getJavaProps() {
        Properties props = System.getProperties();
        return props;
    }

    @CommandDef(command = "env.properties", description = "Dump the config", resultMapper = MapMapping.class)
    public static TreeMap<String, String> getConfig() {
        HTProperties props = HTProperties.getProperties();
        return props.getMap();
    }

    @CommandDef(command = "quit", description = "exit the session")
    public static final boolean quit(@DebugArgAno(keyName = "session",
            description = "session",
            defaultValue = "",
            argType = ArgType.Session) CommandSession session) {
        session.exitSession();
        return true;
    }

    public static final int getCPUCores() {
        return Runtime.getRuntime().availableProcessors();
    }
}
