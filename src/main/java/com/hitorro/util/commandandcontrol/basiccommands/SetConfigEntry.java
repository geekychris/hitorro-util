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
package com.hitorro.util.commandandcontrol.basiccommands;

import com.fasterxml.jackson.databind.JsonNode;
import com.hitorro.util.cmdline.ConfigChangeWatcher;
import com.hitorro.util.commandandcontrol.Command;
import com.hitorro.util.commandandcontrol.CommandSession;
import com.hitorro.util.commandandcontrol.Response;
import com.hitorro.util.commandandcontrol.RestOperations;
import com.hitorro.util.commandandcontrol.ano.CommandArgument;
import com.hitorro.util.commandandcontrol.ano.CommandDef;
import com.hitorro.util.core.Env;
import com.hitorro.util.core.params.HTProperties;
import com.hitorro.util.io.FileUtil;
import com.hitorro.util.json.keys.StringProperty;

import java.io.*;


@CommandDef(command = "env.setconfig", description = "Set a key in the configs")
public class SetConfigEntry extends Command {
    @CommandArgument(required = true)
    public static final StringProperty KeyProp = new StringProperty("key", "property key", null);
    @CommandArgument(required = true)
    public static final StringProperty ValueProp = new StringProperty("value", "property value", null);

    @Override
    public boolean execute(String rawValue, JsonNode args, Response response, CommandSession session, RestOperations operation) throws Exception {
        String key = KeyProp.apply(args);
        String value = ValueProp.apply(args);
        File f = Env.getSavedProps();
        HTProperties map = new HTProperties();
        if (FileUtil.notNullAndExists(f)) {

            InputStream is;
            try {
                is = FileUtil.getBufferedFileInputStream(f);
                HTProperties.load(is, map);
            } catch (FileNotFoundException e) {
                this.writeSimpleError(response, "Unable to read saved properties file %s %e", e, e);
                return false;
            } catch (IOException e) {
                this.writeSimpleError(response, "Unable to read saved properties file %s %e", e, e);
                return false;
            }

        }
        map.put(key.toLowerCase(), value);
        File tmp = FileUtil.getTempFileWithFromPeerFileWithExtension(f, "proptmp");
        OutputStream os = null;
        try {
            os = FileUtil.getBufferedFileOutputStream(tmp);
            map.write(os);
            FileUtil.swap(tmp, f);
            tmp.delete();
            // we saved, now lets force a load
            ConfigChangeWatcher.forceReload();
            this.writeSuccess(response, "Updated configs");
        } catch (FileNotFoundException e) {
            this.writeSimpleError(response, "Unable to write saved properties file %s %e", e, e);
            return false;
        }

        return false;
    }
}
