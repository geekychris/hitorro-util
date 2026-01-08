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
package com.hitorro.util.testframework;

import com.hitorro.util.commandandcontrol.CSVResponse;
import com.hitorro.util.commandandcontrol.LogResponse;
import com.hitorro.util.commandandcontrol.PackingResponse;
import com.hitorro.util.commandandcontrol.TeeResponse;
import com.hitorro.util.commandandcontrol.xml.XMLJUnitResponse;
import com.hitorro.util.core.Env;
import com.hitorro.util.core.ListUtil;
import com.hitorro.util.core.Log;
import com.hitorro.util.core.events.EventListener;
import com.hitorro.util.core.events.LocalEventHub;
import com.hitorro.util.core.string.Fmt;
import com.hitorro.util.core.string.StringUtil;
import com.hitorro.util.io.FileUtil;
import com.hitorro.util.log.Logger;
import com.hitorro.util.startupframework.ExitReasonObject;

import java.io.File;
import java.util.List;

/**
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
