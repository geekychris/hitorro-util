package ht.util.startupframework;

import com.fasterxml.jackson.databind.JsonNode;
import ht.jsontypesystem.JVS;
import ht.jsontypesystem.propreaders.JVSProperties;
import ht.util.core.Log;
import ht.util.core.classes.ClassUtil;
import ht.util.core.string.Fmt;
import ht.util.core.string.StringUtil;
import ht.util.json.keys.StringProperty;
import ht.util.json.keys.propaccess.PropaccessError;
import ht.util.startupframework.phases.*;

import java.util.ArrayList;
import java.util.List;

@ServiceDefinition(dependentService = {},
        shortName = "server",
        description = "Service startup service",
        debugCommands = {},
        generatedServices = true
)
public class HTServer extends RunnableService {
    public static final StringProperty NodeId = new StringProperty("node", "id of node if server is a multinodel server", null);
    @ServiceProperty
    public final StringProperty ServerType = new StringProperty("servertype", "type of server to start", null);
    private String serverType;
    private JsonNode serverProps = null;

    public List<Class> getDependentService() {
        serverType = ServerType.apply();
        JVS jvs = JVSProperties.getProperties();
        String server = Fmt.S("services.%s", serverType);
        try {
            serverProps = jvs.get(server);
        } catch (PropaccessError propaccessError) {
            return null;
        }
        if (serverProps != null) {
            List<Class> dependencies = new ArrayList<Class>();
            for (JsonNode e : serverProps) {
                String s = e.textValue();
                Class clazz = ClassUtil.getClassForName(s, null);
                if (clazz != null) {
                    dependencies.add(clazz);
                } else {
                    Log.servicecontext.fatal("Dependent service %s does not have a corresponding class to initialize", s);
                }
            }
            return dependencies;
        }
        return null;
    }

    @ServiceInit
    public String init(boolean dbInit, final boolean upgrading, final long currentVersion, final long targetVersion) {
        if (serverProps == null || serverProps.size() == 0) {
            return Fmt.S("Unable to initialize HTServer, service %s is not defined in the services file.", serverType);
        }
        return null;
    }

    public String run() {
        ServiceContext.waitForDeInit();
        return null;
    }

    @ServiceDeInit
    public String deInit() {
        return null;
    }

    public String getShortName() {
        String nodeId = NodeId.apply();
        if (StringUtil.nullOrEmptyOrBlankString(nodeId)) {
            return Fmt.S("Server-%s", ServerType.apply());
        }

        return Fmt.S("%s-%s", ServerType.apply(), nodeId);
    }

    @ServiceStart
    public String start(boolean dbInit) {
        return null;
    }

}
