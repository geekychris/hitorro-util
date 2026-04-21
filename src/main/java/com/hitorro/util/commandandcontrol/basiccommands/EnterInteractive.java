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
import com.hitorro.util.commandandcontrol.*;
import com.hitorro.util.commandandcontrol.ano.CommandArgument;
import com.hitorro.util.commandandcontrol.ano.CommandDef;
import com.hitorro.util.core.string.StringUtil;
import com.hitorro.util.json.keys.StringProperty;


@CommandDef(command = "interactive", description = "Enter interactive input with a command that supports interactive commands")
public class EnterInteractive extends com.hitorro.util.commandandcontrol.Command {
    @CommandArgument(required = true)
    public static StringProperty cmd = new StringProperty("command", "command to interact with", null);

    public boolean execute(String rawValue, JsonNode args, com.hitorro.util.commandandcontrol.Response response, com.hitorro.util.commandandcontrol.CommandSession session, com.hitorro.util.commandandcontrol.RestOperations operation) throws Exception {
        String c = cmd.apply(args);
        com.hitorro.util.commandandcontrol.Command command = com.hitorro.util.commandandcontrol.CommandRegistry.getRegistry().get(c);
        if (command == null) {
            this.writeSimpleError(response, "Command %s was not found", c);
            return false;
        }
        if (StringUtil.nullOrEmptyOrBlankString(command.interactiveArgument())) {
            this.writeSimpleError(response, "Command %s is not setup for interactive input", c);
            return false;
        }
        session.setInteractiveCommand(command);
        this.writeSuccess(response, "Command set, enter \"exit\" to exit interactive mode");
        return true;
    }
}
