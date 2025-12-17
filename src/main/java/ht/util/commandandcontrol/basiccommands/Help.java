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
package ht.util.commandandcontrol.basiccommands;

import ht.jsontypesystem.JVS;
import ht.util.commandandcontrol.*;
import ht.util.commandandcontrol.ano.CommandArgument;
import ht.util.commandandcontrol.ano.CommandDef;
import ht.util.commandandcontrol.ano.RespColumn;
import ht.util.commandandcontrol.ano.ResponseDefinition;
import ht.util.core.GenericKeyValue;
import ht.util.core.string.StringUtil;
import ht.util.json.keys.BaseMappingProperty;
import ht.util.json.keys.StringProperty;

import java.util.ArrayList;
import java.util.List;

/**
 * Copyright (c) 2003 - present HiTorro All rights reserved.
 * <p/>
 * User: chris
 */
@CommandDef(command = "help", description = "Get help about a specific debug command.")
public class Help extends Command {
    @CommandArgument(required = false)
    public static final StringProperty Command = new StringProperty("command", "Command to get help about", null);
    @CommandArgument(required = false)
    public static final StringProperty StartsWith = new StringProperty("startswith", "arguments that start with", null);


    @ResponseDefinition(command = "help",
            rowname = "command",
            columns = {@RespColumn(name = "Argument", lName = "arg"),
                    @RespColumn(name = "Required", lName = "cmd"),
                    @RespColumn(name = "Type", lName = "type"),
                    @RespColumn(name = "Description", lName = "desc"),
                    @RespColumn(name = "ValidValue", lName = "validval"),
                    @RespColumn(name = "ValidValueDescription", lName = "validvaldesc")})
    private ResponseShape header = new ResponseShape();

    @Override
    public boolean execute(String rawValue, JVS args, Response response, CommandSession session, RestOperations operation) throws Exception {
        String command = Command.apply(args);
        String startsWith = StartsWith.apply(args);
        if (command == null) {
            command = rawValue;
        }

        Command c = null;
        if (!StringUtil.nullOrEmptyOrBlankString(command)) {
            c = CommandRegistry.getRegistry().get(command);
        }
        if (c == null) {
            CommandRegistry.listCommands(response, session);
        } else {
            response.addBannerRow(c.getCommand());
            response.addBannerRow(c.getDescription());
            List<DebugCommandArg> argsList = c.getArguments();
            if (!StringUtil.nullOrEmptyString(startsWith)) {
                List<DebugCommandArg> argsList2 = new ArrayList();
                for (DebugCommandArg arg : argsList) {
                    if (arg.getName().startsWith(startsWith)) {
                        argsList2.add(arg);
                    }
                }
                argsList = argsList2;
            }
            response.setResponseShape(header);

            for (DebugCommandArg arg : argsList) {
                BaseMappingProperty key = arg.getJsonPropertyKey();
                List<GenericKeyValue> kvs = null;
                String keyString = "";
                String valueString = "";
                if (key.getHasValidationList()) {
                    kvs = key.getValidationList();
                    if (kvs.size() > 0) {
                        GenericKeyValue<String, String> kvp = kvs.get(0);
                        keyString = kvp.getKey();
                        valueString = kvp.getValue();
                    }
                }
                response.addRow(arg.getName(),
                        Boolean.toString(arg.getRequired()),
                        key.getPropertyType(),
                        key.getDescription(),
                        keyString,
                        valueString);
                if (kvs != null) {
                    for (int i = 1; i < kvs.size() - 1; i++) {
                        GenericKeyValue<String, String> kvp = kvs.get(0);
                        keyString = kvp.getKey();
                        valueString = kvp.getValue();
                        response.addRow("", "", "", "",
                                keyString,
                                valueString);
                    }
                }
            }
            response.end();
        }
        return true;
    }

}
