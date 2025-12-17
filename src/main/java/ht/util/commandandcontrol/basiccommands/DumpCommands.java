package ht.util.commandandcontrol.basiccommands;

import ht.jsontypesystem.JVS;
import ht.util.commandandcontrol.*;
import ht.util.commandandcontrol.ano.CommandDef;

/**
 * Copyright (c) 2003 - present HiTorro All rights reserved.
 * <p/>
 * User: chris
 */
@CommandDef(command = "commands", description = "List the available commands")
public class DumpCommands extends Command {
    @Override
    public boolean execute(String rawValue, JVS args, Response response, CommandSession session, RestOperations operation) throws Exception {
        CommandRegistry.listCommands(response, session);
        return true;
    }
}
