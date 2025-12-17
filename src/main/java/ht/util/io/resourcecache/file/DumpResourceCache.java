package ht.util.io.resourcecache.file;

import ht.jsontypesystem.JVS;
import ht.util.commandandcontrol.*;
import ht.util.commandandcontrol.ano.CommandDef;
import ht.util.commandandcontrol.ano.RespColumn;
import ht.util.commandandcontrol.ano.ResponseDefinition;
import ht.util.core.GenericKeyValue;

import java.util.List;

/**
 * Copyright (c) 2003 - present HiTorro All rights reserved.
 * <p/>
 * User: chris
 */
@CommandDef(command = "resourcecache.listFiles", description = "Dump the resource cache contents.")
public class DumpResourceCache extends Command {
    @ResponseDefinition(command = "resources",
            rowname = "resource",
            columns = {@RespColumn(name = "Resource", lName = "resource"),
                    @RespColumn(name = "Version", lName = "version")})
    private ResponseShape header = new ResponseShape();

    public boolean execute(String rawValue, JVS args, Response response, CommandSession session, RestOperations operation) throws Exception {
        List<GenericKeyValue> list = ResourceCache.getCache().getCacheDetails();
        if (list == null) {
            this.writeSimpleError(response, "Unable to get resource cache listFiles.");
        }
        this.writeKeyValue(response, header, list);
        return true;
    }
}
