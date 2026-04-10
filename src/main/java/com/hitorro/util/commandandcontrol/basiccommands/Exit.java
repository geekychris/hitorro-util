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
import com.hitorro.util.core.Env;
import com.hitorro.util.core.events.LocalEventHub;
import com.hitorro.util.json.keys.BooleanProperty;
import com.hitorro.util.json.keys.IntegerProperty;
import com.hitorro.util.json.keys.StringProperty;
import com.hitorro.util.log.Logger;
import com.hitorro.util.startupframework.ExitReasonObject;


@CommandDef(command = "exit", description = "exit the session or the vm")
public class Exit extends Command {
    @CommandArgument(required = true)
    private BooleanProperty s_exitVm = new BooleanProperty("vm", "exit the vm", false);
    @CommandArgument(required = false)
    private BooleanProperty s_forceExit = new BooleanProperty("force", "exit the vm and do not wait for the services to shutdown", false);
    @CommandArgument(required = false)
    private IntegerProperty ExitCode = new IntegerProperty("code", "Exit code to be used", -1);
    @CommandArgument(required = false)
    private StringProperty Reason = new StringProperty("reason", "Reason given for exit", "User invoked exit");

    @Override
    public boolean execute(String rawValue, JsonNode args, Response response, CommandSession session, RestOperations operation) throws Exception {
        boolean exitVm = s_exitVm.apply(args);
        boolean force = s_forceExit.apply(args);
        int exitCode = ExitCode.apply(args);
        String reason = Reason.apply(args);
        if (exitVm) {

            if (force) {
                Env.exitSystem(reason, exitCode);
            } else {
                ExitReasonObject reasonObject = new ExitReasonObject(reason, exitCode);
                LocalEventHub.get().event(Logger.ExitEvent, "", reasonObject);
                this.writeSuccess(response, "Notified exit handler, use force=true if exit does not occur");
            }
        } else {
            session.exitSession();
        }
        return true;
    }
}
