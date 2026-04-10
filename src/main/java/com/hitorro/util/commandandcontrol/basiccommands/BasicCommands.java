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

import com.hitorro.util.cmdline.BaseCommandLine;
import com.hitorro.util.commandandcontrol.CommandSession;
import com.hitorro.util.commandandcontrol.InfoLevel;
import com.hitorro.util.commandandcontrol.Response;
import com.hitorro.util.commandandcontrol.ano.ArgType;
import com.hitorro.util.commandandcontrol.ano.CommandDef;
import com.hitorro.util.commandandcontrol.ano.DebugArgAno;
import com.hitorro.util.commandandcontrol.responsemappings.KeyValuePairMapping;
import com.hitorro.util.core.GenericKeyValue;
import com.hitorro.util.core.string.Fmt;
import com.hitorro.util.core.string.StringUtil;
import com.hitorro.util.json.keys.StringProperty;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;


public class BasicCommands {
    @CommandDef(command = "assume",
            description = "assume the user and store the command history to disk.  Also assuming user will load any history from disk if it exists.",
            isInternal = false)
    public static String assume(@DebugArgAno(propType = StringProperty.class, keyName = "user",
            description = "username", defaultValue = "chris") String username,
                                @DebugArgAno(keyName = "session",
                                        description = "session",
                                        defaultValue = "",
                                        argType = ArgType.Session) CommandSession session) {
        try {
            if (session.assumeUser(username)) {
                return Fmt.S("Assumed user %s", username);
            }
        } catch (FileNotFoundException e) {
            return Fmt.S("Unable to assume user %s %s %e", username, e, e);
        } catch (UnsupportedEncodingException e) {
            return Fmt.S("Unable to assume user %s %s %e", username, e, e);
        }
        return Fmt.S("Unable to assume user %s", username);
    }

    @CommandDef(command = "set", description = "set a session variable", resultMapper = KeyValuePairMapping.class)
    public static List<GenericKeyValue> setSessionVar(@DebugArgAno(propType = StringProperty.class, keyName = "name",
            description = "variable name", defaultValue = "") String name,
                                                      @DebugArgAno(propType = StringProperty.class, keyName = "value",
                                                              description = "variable value", defaultValue = "", mustExist = false) String val,
                                                      @DebugArgAno(keyName = "response",
                                                              description = "response",
                                                              defaultValue = "",
                                                              argType = ArgType.Response) Response response,
                                                      @DebugArgAno(keyName = "session",
                                                              description = "session",
                                                              defaultValue = "",
                                                              argType = ArgType.Session) CommandSession session) {

        if (StringUtil.nullOrEmptyString(val)) {
            session.removeVar(name);
            response.addInfo(InfoLevel.Info, "variable removed");
        } else {
            session.setVar(name, val);
        }
        return session.getVars();
    }

    @CommandDef(command = "script", description = "copy output to file")
    public static String script(@DebugArgAno(propType = StringProperty.class, keyName = "script",
            description = "name of file", defaultValue = "") String scr,
                                @DebugArgAno(keyName = "session",
                                        description = "session",
                                        defaultValue = "",
                                        argType = ArgType.Session) CommandSession session) {
        if (StringUtil.nullOrEmptyOrBlankString(scr)) {
            File old = session.getScript();
            if (old == null) {
                return "Script disabled";
            } else {
                session.setScript(null);
                return Fmt.S("Disabled scripting to file: %s", old.getAbsolutePath());
            }
        } else {
            File f = new File(scr);
            session.setScript(f);
            return Fmt.S("Enabled scripting to file: %s", f.getAbsolutePath());
        }
    }

    @CommandDef(command = "env.uptime", description = "Get how long this process has been running", resultMapper = KeyValuePairMapping.class)
    public static List<GenericKeyValue> uptime() {
        List<GenericKeyValue> list = new ArrayList();
        list.add(new GenericKeyValue("Start", Fmt.formatDateTime(BaseCommandLine.getCommandLine().processStartTime)));
        list.add(new GenericKeyValue("Delta", Fmt.formatDateTimeDelta(System.currentTimeMillis(), BaseCommandLine.getCommandLine().processStartTime)));
        return list;
    }
}
