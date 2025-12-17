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
package com.hitorro.util.io.resourcecache.basefile;

import com.hitorro.jsontypesystem.JVS;
import com.hitorro.util.basefile.fs.BaseFile;
import com.hitorro.util.commandandcontrol.Command;
import com.hitorro.util.commandandcontrol.CommandSession;
import com.hitorro.util.commandandcontrol.Response;
import com.hitorro.util.commandandcontrol.RestOperations;
import com.hitorro.util.commandandcontrol.ano.CommandArgument;
import com.hitorro.util.commandandcontrol.ano.CommandDef;
import com.hitorro.util.core.Env;
import com.hitorro.util.core.opers.AlwaysTrueOperator;
import com.hitorro.util.json.keys.IntegerProperty;
import com.hitorro.util.json.keys.StringProperty;

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
