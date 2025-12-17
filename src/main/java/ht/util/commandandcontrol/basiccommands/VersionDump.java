package ht.util.commandandcontrol.basiccommands;

import ht.jsontypesystem.JVS;
import ht.util.commandandcontrol.Command;
import ht.util.commandandcontrol.CommandSession;
import ht.util.commandandcontrol.Response;
import ht.util.commandandcontrol.RestOperations;
import ht.util.commandandcontrol.ano.CommandDef;
import ht.util.core.GenericKeyValue;
import ht.util.versioning.Version;

import java.util.List;

/**
 * Copyright (c) 2003 - present HiTorro All rights reserved.
 * <p/>
 * User: chris
 */
@CommandDef(command = "env.version", description = "Dump the build, vm and os version information")
public class VersionDump extends Command {
    public boolean execute(String rawValue, JVS args, Response response, CommandSession session, RestOperations operation) throws Exception {
        List<GenericKeyValue> list = new Version().getValues();
        writeKeyValue(response, getKVShape(), list);
        return true;
    }
}
