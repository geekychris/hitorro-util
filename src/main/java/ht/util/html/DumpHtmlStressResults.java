/*
 * Copyright (c) 2006-2025 Chris Collins
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
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
