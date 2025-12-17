package ht.util.commandandcontrol.basiccommands;

import ht.jsontypesystem.JVS;
import ht.util.commandandcontrol.CommandSession;
import ht.util.commandandcontrol.Response;
import ht.util.commandandcontrol.RestOperations;
import ht.util.commandandcontrol.ano.CommandDef;
import ht.util.json.keys.StringProperty;
import ht.util.log.Logger;

/**
 * Copyright (c) 2003 - present HiTorro All rights reserved.
 * <p/>
 * User: chris
 */
@CommandDef(command = "env.setloglevel", description = "Set the logging level")
public class SetLogLevel extends DumpLogLevels {
    public static final StringProperty Level = new StringProperty("level", "category:level,category2:level...", null);

    public SetLogLevel() {
        this.addArgument(true, Level);
    }

    @Override
    public boolean execute(String rawValue, JVS args, Response response, CommandSession session, RestOperations operation) throws Exception {
        Logger.setLogLevels(Level.apply(args));
        super.execute(rawValue, args, response, session, operation);
        return false;
    }
}
