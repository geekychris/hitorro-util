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
package com.hitorro.util.startupframework.steps;

import com.fasterxml.jackson.databind.JsonNode;
import com.hitorro.util.core.params.GlobalProperties;
import com.hitorro.util.core.ArrayUtil;
import com.hitorro.util.core.Env;
import com.hitorro.util.core.Log;
import com.hitorro.util.core.error.ErrorCode;
import com.hitorro.util.core.string.Fmt;
import com.hitorro.util.core.string.StringBuilderUtil;
import com.hitorro.util.core.string.StringUtil;
import com.hitorro.util.io.FileUtil;
import com.hitorro.util.startupframework.ServiceContext;
import com.hitorro.util.startupframework.ServiceWrapper;

import java.io.File;
import java.io.IOException;

/**
 * Ensure we have copied all UI resources to their correct locations for picking up at runtime.  This is primarly for
 * tapestry 4 but can be used as a template for anything else that has to be copied out of a private resource bundle for
 * runtime usage.
 */
public class InitUIDirs implements ServiceStep {
    public static final String EventName = "InitUIDirs";

    private static void ensureCorrectUIResource(String buildNumber, File uiResource) {
        File versionFile = new File(uiResource, "platversion.txt");
        boolean writeVersionFile = false;
        if (versionFile.exists()) {
            StringBuilder builder = new StringBuilder();
            try {
                StringBuilderUtil.readFileIntoBuilder(builder, versionFile);
                String ver = builder.toString();
                if (!buildNumber.equals(ver)) {
                    // we dont have the same version running,
                    // NUKE!
                    FileUtil.deleteDirectoryContent(uiResource, false);
                    writeVersionFile = true;
                }
            } catch (IOException e) {
                Log.servicecontext.error("%s %e", e, e);
            }
        } else {
            writeVersionFile = true;
        }
        if (writeVersionFile) {
            try {
                FileUtil.writeToFile(versionFile, buildNumber);
            } catch (IOException e) {
                Log.servicecontext.error("Unable to write version file %s %e", e, e);
            }
        }
    }

    public static String ensureUIResourceDependency(ServiceWrapper service, File buildDir, File resourceDir) {
        String[] resources = service.getUIDirectories();
        if (!ArrayUtil.nullOrEmpty(resources)) {
            for (String resource : resources) {
                File dest = new File(resourceDir, resource);
                File src = new File(buildDir, resource);
                if (FileUtil.nullOrNotExist(src)) {
                    Log.servicecontext.error("Source unknown %s", src);
                    continue;
                }
                if (!dest.exists()) {
                    // doesnt exist, lets copy it
                    try {
                        FileUtil.copyDirectory(src, dest);
                    } catch (IOException e) {
                        Log.servicecontext.error("Unable to copy %s to %s, %s %e", src, dest, e, e);
                    }
                }
            }
        }
        return null;
    }

    @Override
    public String getPostStepEvent() {
        return EventName;
    }

    @Override
    public String getPhaseName() {
        return "InitUIDirs";
    }

    @Override
    public ErrorCode execute(boolean initDb) {
        String v = null;
        String n = null;
        try {
            v = GlobalProperties.getString("build.version");
            n = GlobalProperties.getString("build.number");
            String buildNumber = Fmt.S("%s.%s", v, n);
            String serverId = Env.getServerId();

            File uiResource = new File(Env.getHome(), Fmt.S("uiresource/%s", serverId));
            File buildDir = new File(Env.getBin(), "build");
            JsonNode props = GlobalProperties.getProperties();
            if (props instanceof com.fasterxml.jackson.databind.node.ObjectNode) {
                ((com.fasterxml.jackson.databind.node.ObjectNode) props).put("ht_resource", uiResource.getAbsolutePath());
            }
            FileUtil.ensureDirectoryExists(uiResource);
            ensureCorrectUIResource(buildNumber, uiResource);

            for (ServiceWrapper module : ServiceContext.getSC().getServices()) {
                String text = ensureUIResourceDependency(module, buildDir, uiResource);
                if (!StringUtil.nullOrEmptyString(text)) {
                    return new ErrorCode(40, "Unable to initialize(UI Resource Init) module %s with error %s",
                            module.getShortName(), text);
                }
            }
        } catch (Exception e) {
            return new ErrorCode(40, "Unable to initialize(UI Resource Init) module %s %e", e, e);
        }

        return null;
    }
}
