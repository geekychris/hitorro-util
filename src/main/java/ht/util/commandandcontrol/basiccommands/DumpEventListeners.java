package ht.util.commandandcontrol.basiccommands;

import ht.jsontypesystem.JVS;
import ht.util.commandandcontrol.*;
import ht.util.commandandcontrol.ano.CommandDef;
import ht.util.commandandcontrol.ano.RespColumn;
import ht.util.commandandcontrol.ano.ResponseDefinition;
import ht.util.core.GenericKeyValue;
import ht.util.core.events.LocalEventHub;

import java.util.List;

/**
 * Copyright (c) 2003 - present HiTorro All rights reserved.
 * <p/>
 * User: chris
 * <p/>
 * List the event listeners registered.
 */
@CommandDef(command = "env.eventlisteners", description = "Dump the registered event listeners.")
public class DumpEventListeners extends Command {
    @ResponseDefinition(command = "listeners",
            rowname = "listener",
            columns = {@RespColumn(name = "Topic", lName = "topic"),
                    @RespColumn(name = "Listener Name", lName = "listenername")})
    private ResponseShape header = new ResponseShape();

    public boolean execute(String rawValue, JVS args, Response response, CommandSession session, RestOperations operation) throws Exception {
        List<GenericKeyValue> list = LocalEventHub.get().getRegisteredListeners();
        this.writeKeyValue(response, header, list);
        return true;
    }
}
