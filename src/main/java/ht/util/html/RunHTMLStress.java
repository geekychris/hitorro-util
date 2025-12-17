package ht.util.html;

import ht.jsontypesystem.JVS;
import ht.util.commandandcontrol.Command;
import ht.util.commandandcontrol.CommandSession;
import ht.util.commandandcontrol.Response;
import ht.util.commandandcontrol.RestOperations;
import ht.util.commandandcontrol.ano.CommandDef;

/**
 * Copyright (c) 2003 - present HiTorro All rights reserved. User: chris Date: Jan 3, 2005 Time: 12:57:32 PM
 */
@CommandDef(command = "test.runhtmlstress", description = "Run html fetching stress")
public class RunHTMLStress extends Command {
    public boolean execute(String rawValue, JVS args, Response response, CommandSession session, RestOperations operation) throws Exception {
        HTMLFetcherStressController controller = HTMLFetcherStressController.s_controller;
        if (HTMLFetcherStressController.s_controller == null) {
            HTMLFetcherStressController.s_controller = new HTMLFetcherStressController();
        }
        HTMLFetcherStressController.s_controller.setInputFile("/pt/url.txt");
        HTMLFetcherStressController.s_controller.startClients();
        return true;
    }
}
