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
package com.hitorro.util.startupframework;

import com.fasterxml.jackson.databind.JsonNode;
import com.hitorro.util.core.params.GlobalProperties;
import com.hitorro.util.core.Log;
import com.hitorro.util.core.classes.ClassUtil;
import com.hitorro.util.core.string.Fmt;
import com.hitorro.util.core.string.StringUtil;
import com.hitorro.util.json.keys.StringProperty;
import com.hitorro.util.json.keys.propaccess.PropaccessError;
import com.hitorro.util.startupframework.phases.ServiceDefinition;

import java.util.ArrayList;
import java.util.List;

@ServiceDefinition(dependentService = {},
        shortName = "server",
        description = "Service startup service",
        debugCommands = {},
        generatedServices = true
)
public class HTServer extends RunnableService {
    public static StringProperty NodeId = new StringProperty("node", "id of node if server is a multinodel server", null);
    @com.hitorro.util.startupframework.phases.ServiceProperty
    public final StringProperty ServerType = new StringProperty("servertype", "type of server to start", null);
    private String serverType;
    private JsonNode serverProps = null;

    public List<Class> getDependentService() {
        serverType = ServerType.apply();
        JsonNode props = GlobalProperties.getProperties();
        String server = Fmt.S("services.%s", serverType);
        try {
            serverProps = props != null ? props.get(server) : null;
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

    @com.hitorro.util.startupframework.phases.ServiceInit
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

    @com.hitorro.util.startupframework.phases.ServiceDeInit
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

    @com.hitorro.util.startupframework.phases.ServiceStart
    public String start(boolean dbInit) {
        return null;
    }

}
