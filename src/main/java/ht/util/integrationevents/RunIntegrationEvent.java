package ht.util.integrationevents;

import ht.jsontypesystem.JVS;
import ht.util.commandandcontrol.Command;
import ht.util.commandandcontrol.CommandSession;
import ht.util.commandandcontrol.Response;
import ht.util.commandandcontrol.RestOperations;
import ht.util.commandandcontrol.ano.CommandArgument;
import ht.util.commandandcontrol.ano.CommandDef;
import ht.util.json.keys.StringProperty;

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
