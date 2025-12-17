package ht.util.html;

import ht.jsontypesystem.JVS;
import ht.util.commandandcontrol.*;
import ht.util.commandandcontrol.ano.CommandDef;
import ht.util.commandandcontrol.ano.RespColumn;
import ht.util.commandandcontrol.ano.ResponseDefinition;

/**
 * Copyright (c) 2003 - present HiTorro All rights reserved. User: chris Date: Jan 3, 2005 Time: 12:29:58 PM
 */
@CommandDef(command = "test.dumphtmlstress", description = "Dump the stress results of the html fetcher")
public class DumpHtmlStressResults extends Command {
    @ResponseDefinition(command = "dumphtmlstress",
            rowname = "result",
            columns = {@RespColumn(name = "Successes", lName = "successes", type = Long.class),
                    @RespColumn(name = "Failures", lName = "failures", type = Integer.class),
                    @RespColumn(name = "Average Time", lName = "avgfetchtime", type = Integer.class)})
    private ResponseShape shape = new ResponseShape();

    public boolean execute(String rawValue, JVS args, Response response, CommandSession session, RestOperations operation) throws Exception {
        HTMLFetcherStressController controller = HTMLFetcherStressController.s_controller;
        if (controller == null) {
            writeSimpleError(response, "Controller not initialized.");
            return false;
        }
        long average = controller.getAverageFetchTime();
        int success = controller.getSuccesses();
        int failures = controller.getFailures();
        response.setResponseShape(shape);
        response.addRow(success, failures, average);
        return true;
    }
}
