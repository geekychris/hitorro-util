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
package com.hitorro.util.integrationevents;

import com.hitorro.jsontypesystem.JVS;
import com.hitorro.util.commandandcontrol.Command;
import com.hitorro.util.commandandcontrol.CommandSession;
import com.hitorro.util.commandandcontrol.Response;
import com.hitorro.util.commandandcontrol.RestOperations;
import com.hitorro.util.commandandcontrol.ano.CommandArgument;
import com.hitorro.util.commandandcontrol.ano.CommandDef;
import com.hitorro.util.json.keys.StringProperty;

/**
 * Copyright (c) 2003 - present HiTorro All rights reserved. User: chris Date: Dec 18, 2006 Time: 6:52:14 PM
 */
@CommandDef(command = "integration.run", description = "Run an integration event")
public class RunIntegrationEvent extends Command {
    @CommandArgument(required = true)
    public static final StringProperty EventName = new StringProperty("event", "Event name", "");

    public boolean execute(String rawValue, JVS args, Response response, CommandSession session, RestOperations operation) throws Exception {
        String event = EventName.apply(args);
        if (!IntegrationEventsContext.getContext().hasEvent(event)) {
            writeSimpleError(response, "Event %s unknown", event);
        }
        boolean flag = IntegrationEventsContext.getContext().runEvent(event);
        if (flag) {
            writeSuccess(response, "Executed");
        } else {
            writeSimpleError(response, "Unable to execute event %s", event);
        }
        return false;
    }
}
