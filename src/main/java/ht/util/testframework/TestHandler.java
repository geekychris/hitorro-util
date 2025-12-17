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
package ht.util.testframework;

import ht.util.commandandcontrol.CSVResponse;
import ht.util.commandandcontrol.LogResponse;
import ht.util.commandandcontrol.PackingResponse;
import ht.util.commandandcontrol.TeeResponse;
import ht.util.commandandcontrol.xml.XMLJUnitResponse;
import ht.util.core.Env;
import ht.util.core.ListUtil;
import ht.util.core.Log;
import ht.util.core.events.EventListener;
import ht.util.core.events.LocalEventHub;
import ht.util.core.string.Fmt;
import ht.util.core.string.StringUtil;
import ht.util.io.FileUtil;
import ht.util.log.Logger;
import ht.util.startupframework.ExitReasonObject;

import java.io.File;
import java.util.List;

/**
 * Copyright (c) 2003 - present HiTorro All rights reserved. User: chris Date: Nov 21, 2006 Time: 8:13:01 PM
 */
class TestHandler implements EventListener {
    public boolean event(String topic, String subTopic, Object args) {
        String level = TestUtil.Level.apply();
        List<String> tests = TestUtil.Test.apply();

        if (StringUtil.nullOrEmptyOrBlankString(level)) {
            Log.test.fatal("Run Level not defined", level);
            exitTest(-1);
            return false;
        }

        String name = Fmt.S("TestServer-%s", level);
        File csvFile = FileUtil.getDatedFileFromPattern(Env.getTestLogDir(), name, "csv");
        File xmlFile = FileUtil.getDatedFileFromPattern(Env.getTestLogDir(), name, "xml");
        FileUtil.ensureParentDirectories(csvFile, true);
        FileUtil.ensureParentDirectories(xmlFile, true);
        CSVResponse csvResponse = new CSVResponse(csvFile);
        PackingResponse logResponse = new PackingResponse(new LogResponse(Log.test), 2);
        XMLJUnitResponse xmlResponse = new XMLJUnitResponse(xmlFile);
        TeeResponse response = new TeeResponse(logResponse, csvResponse);
        TeeResponse nestedResponse = new TeeResponse(response, xmlResponse);


        if (RunLevel.getFilterByName(level) == null) {
            Log.test.fatal("Run Level %s is unknown", level);
            exitTest(-1);
            return false;
        }

        /*   construct test-suite   */
        int exitCode = 0;
        try {
            if (ListUtil.notNullAndContainsRows(tests)) {
                for (String test : tests) {
                    exitCode |= TestUtil.runTest(test, null, nestedResponse);
                }
            } else {
                exitCode = TestUtil.runTest(null, null, nestedResponse);
            }
        } catch (TestException e) {
            Log.test.fatal(e.getMessage());
            exitTest(-1);
        }

        if (TestUtil.Shutdown.apply()) {
            exitTest(exitCode);
        }

        // exit the server now and provide an exit code appropriate for

        return true;
    }


    public String eventName() {
        return "TestHandler";
    }


    public boolean runAsync() {
        return true;
    }


    private void exitTest(int exitCode) {
        ExitReasonObject reasonObject = new ExitReasonObject("TestServer orderly shutting down", exitCode);
        LocalEventHub.get().event(Logger.ExitEvent, "", reasonObject);
    }
}
