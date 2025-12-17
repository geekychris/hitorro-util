package ht.util.startupframework.steps;

import ht.jsontypesystem.propreaders.JVSProperties;
import ht.util.core.ArrayUtil;
import ht.util.core.Env;
import ht.util.core.Log;
import ht.util.core.error.ErrorCode;
import ht.util.core.string.Fmt;
import ht.util.core.string.StringBuilderUtil;
import ht.util.core.string.StringUtil;
import ht.util.io.FileUtil;
import ht.util.json.keys.propaccess.PropaccessError;
import ht.util.startupframework.ServiceContext;
import ht.util.startupframework.ServiceWrapper;

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
            v = JVSProperties.getProperties().getString("build.version");
            n = JVSProperties.getProperties().getString("build.number");
            String buildNumber = Fmt.S("%s.%s", v, n);
            String serverId = Env.getServerId();

            File uiResource = new File(Env.getHome(), Fmt.S("uiresource/%s", serverId));
            File buildDir = new File(Env.getBin(), "build");
            JVSProperties.getProperties().set("ht_resource", uiResource.getAbsolutePath());
            String r = JVSProperties.getProperties().getString("ht_resource");
            FileUtil.ensureDirectoryExists(uiResource);
            ensureCorrectUIResource(buildNumber, uiResource);

            for (ServiceWrapper module : ServiceContext.getSC().getServices()) {
                String text = ensureUIResourceDependency(module, buildDir, uiResource);
                if (!StringUtil.nullOrEmptyString(text)) {
                    return new ErrorCode(40, "Unable to initialize(UI Resource Init) module %s with error %s",
                            module.getShortName(), text);
                }
            }
        } catch (PropaccessError e) {
            return new ErrorCode(40, "Unable to initialize(UI Resource Init) module %s %e", e, e);
        }

        return null;
    }
}
