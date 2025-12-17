package ht.util.integrationevents;

import ht.jsontypesystem.JVS;
import ht.util.commandandcontrol.*;
import ht.util.commandandcontrol.ano.CommandDef;
import ht.util.commandandcontrol.ano.RespColumn;
import ht.util.commandandcontrol.ano.ResponseDefinition;

import java.util.List;

/**
 * Copyright (c) 2003 - present HiTorro All rights reserved. User: chris Date: Dec 18, 2006 Time: 6:58:46 PM
 */

@CommandDef(command = "integration.listFiles", description = "List the available integration events")
public class ListIntegrationEvents extends Command {
    @ResponseDefinition(command = "integrations",
            rowname = "event",
            columns = {@RespColumn(name = "EventName", lName = "name")})
    private ResponseShape shape = new ResponseShape();

    public boolean execute(String rawValue, JVS args, Response response, CommandSession session, RestOperations operation) throws Exception {
        List<String> names = IntegrationEventsContext.getContext().getEventNames();
        response.setResponseShape(shape);
        for (String name : names) {
            response.addRow(name);
        }
        response.end();
        return true;
    }
}
