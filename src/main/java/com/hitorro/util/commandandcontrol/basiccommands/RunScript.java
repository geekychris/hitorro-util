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
import com.hitorro.util.commandandcontrol.Command;
import com.hitorro.util.commandandcontrol.CommandSession;
import com.hitorro.util.commandandcontrol.Response;
import com.hitorro.util.commandandcontrol.RestOperations;
import com.hitorro.util.commandandcontrol.ano.CommandArgument;
import com.hitorro.util.commandandcontrol.ano.CommandDef;
import com.hitorro.util.json.keys.BooleanProperty;
import com.hitorro.util.json.keys.StringProperty;

import java.io.File;


@CommandDef(command = "run", description = "run a script")
public class RunScript extends Command {
    @CommandArgument(required = true)
    public static StringProperty Script = new StringProperty("script", "Script to executed", null);

    @CommandArgument(required = false)
    public static BooleanProperty SendToResponse = new BooleanProperty("toresponse", "send output to current response stream", false);

    public boolean execute(String rawValue, JsonNode args, Response response, CommandSession session, RestOperations operation) throws Exception {
        String script = Script.apply(args);
        File f = session.findScriptFile(script);
        if (f == null) {
            this.writeSimpleError(response, "Unable to find file %s", script);
            return false;
        }
        if (SendToResponse.apply(args)) {
            session.executeScriptToResponse(f, response);
        } else {
            session.executeScript(session, f);
        }

        writeSuccess(response, "executed script %s", f.getAbsolutePath());
        return false;
    }
}
