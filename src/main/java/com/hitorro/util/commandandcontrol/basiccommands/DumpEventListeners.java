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

import com.hitorro.jsontypesystem.JVS;
import com.hitorro.util.commandandcontrol.*;
import com.hitorro.util.commandandcontrol.ano.CommandDef;
import com.hitorro.util.commandandcontrol.ano.RespColumn;
import com.hitorro.util.commandandcontrol.ano.ResponseDefinition;
import com.hitorro.util.core.GenericKeyValue;
import com.hitorro.util.core.events.LocalEventHub;

import java.util.List;

/**

 * List the event listeners registered.
 */
@CommandDef(command = "env.eventlisteners", description = "Dump the registered event listeners.")
public class DumpEventListeners extends com.hitorro.util.commandandcontrol.Command {
    @ResponseDefinition(command = "listeners",
            rowname = "listener",
            columns = {@RespColumn(name = "Topic", lName = "topic"),
                    @RespColumn(name = "Listener Name", lName = "listenername")})
    private com.hitorro.util.commandandcontrol.ResponseShape header = new com.hitorro.util.commandandcontrol.ResponseShape();

    public boolean execute(String rawValue, JVS args, com.hitorro.util.commandandcontrol.Response response, com.hitorro.util.commandandcontrol.CommandSession session, com.hitorro.util.commandandcontrol.RestOperations operation) throws Exception {
        List<GenericKeyValue> list = LocalEventHub.get().getRegisteredListeners();
        this.writeKeyValue(response, header, list);
        return true;
    }
}
