package ht.util.io.resourcecache.basefile;

import ht.jsontypesystem.JVS;
import ht.util.basefile.fs.BaseFile;
import ht.util.commandandcontrol.Command;
import ht.util.commandandcontrol.CommandSession;
import ht.util.commandandcontrol.Response;
import ht.util.commandandcontrol.RestOperations;
import ht.util.commandandcontrol.ano.CommandArgument;
import ht.util.commandandcontrol.ano.CommandDef;
import ht.util.core.Env;
import ht.util.core.opers.AlwaysTrueOperator;
import ht.util.json.keys.IntegerProperty;
import ht.util.json.keys.StringProperty;

import java.io.File;

@CommandDef(command = "basefileresourcecache.install", description = "Install a resource from disk")
public class BaseFileInstallResourceCacheCommand extends Command {
    @CommandArgument(required = true)
    public static final StringProperty Source = new StringProperty("source", "source file or directory (directory only contents are installed)", null);
    @CommandArgument(required = true)
    public static final StringProperty Resource = new StringProperty("resource", "Name of resource to install as", null);
    @CommandArgument(required = true)
    public static final IntegerProperty Major = new IntegerProperty("major", "Major version number", 1);
    @CommandArgument(required = true)
    public static final IntegerProperty Minor = new IntegerProperty("minor", "Minor version number", 0);
    @CommandArgument(required = true)
    public static final IntegerProperty Patch = new IntegerProperty("patch", "Patch version number", 0);

    public boolean execute(String rawValue, JVS args, Response response, CommandSession session, RestOperations operation) throws Exception {
        String file = Source.apply(args);
        String resource = Resource.apply(args);
        int major = Major.apply(args);
        int minor = Minor.apply(args);
        int patch = Patch.apply(args);
        BaseFile fileF = Env.getBaseFile(new File(file));
        if (!BaseFile.notNullAndExists(fileF)) {
            this.writeSimpleError(response, "File or directory %s does not exist", file);
            return false;
        }


        BaseFileResourceContext c = BaseFileResourceCache.getCache().getTempResourceContext(resource,
                major, minor, patch);
        if (fileF.isFile()) {
            BaseFile f = c.getPath().getChild(fileF.getName());
            fileF.copy(f);
            c.commit();
            this.writeSuccess(response, "Wrote %s as resource %s %s.%s.%s", file, resource, major, minor, patch);
            return true;
        } else {
            if (!fileF.copyDirectory(c.getPath(), AlwaysTrueOperator.oper)) {
                this.writeSuccess(response, "Wrote directory %s as resource %s %s.%s.%s", file, resource, major, minor, patch);
                c.commit();
                return true;
            }
        }
        this.writeSimpleError(response, "Unable to copy content %s ", file);
        return false;
    }
}
