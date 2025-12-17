package ht.util.commandandcontrol.basiccommands;

import ht.jsontypesystem.JVS;
import ht.util.commandandcontrol.Command;
import ht.util.commandandcontrol.CommandSession;
import ht.util.commandandcontrol.Response;
import ht.util.commandandcontrol.RestOperations;
import ht.util.commandandcontrol.ano.CommandArgument;
import ht.util.commandandcontrol.ano.CommandDef;
import ht.util.json.keys.BooleanProperty;
import ht.util.json.keys.StringProperty;

import java.io.File;

/**
 * Copyright (c) 2003 - present HiTorro All rights reserved.
 * <p/>
 * User: chris
 */
@CommandDef(command = "run", description = "run a script")
public class RunScript extends Command {
    @CommandArgument(required = true)
    public static final StringProperty Script = new StringProperty("script", "Script to executed", null);

    @CommandArgument(required = false)
    public static final BooleanProperty SendToResponse = new BooleanProperty("toresponse", "send output to current response stream", false);

    public boolean execute(String rawValue, JVS args, Response response, CommandSession session, RestOperations operation) throws Exception {
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
