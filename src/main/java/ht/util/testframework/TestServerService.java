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

import ht.util.core.events.LocalEventHub;
import ht.util.startupframework.ServiceContext;
import ht.util.startupframework.phases.ServiceDefinition;

import java.util.ArrayList;
import java.util.List;

/**
 * Copyright (c) 2003 - present HiTorro All rights reserved. User: chris Date: Oct 23, 2006 Time: 11:05:14 AM
 * <p/>
 * On startup of the server run unit tests defined at the provided run level and then exit. A positive exit code means
 * success and a negative exit code means failure. The log output it sent to the logs directory as
 * TestServer-<level>-<date>.csv
 * <p/>
 * You need to define level=smoke|full|stress to make this guy run
 */
@ServiceDefinition(dependentService = {},
        shortName = "test",
        description = "Test service",
        debugCommands = {},
        typeManagedClasses = {},
        uiDirectories = {},
        dependentServiceInterfaces = {})
public class TestServerService {
    private static TestServerService service;
    private TestHandler m_handler = new TestHandler();
    private List<String> startsWith = new ArrayList();

    public static TestServerService getInstance() {
        return service;
    }

    public String init(boolean dbInit, final boolean upgrading, final long currentVersion, final long targetVersion) {
        service = this;
        LocalEventHub.get().addEventListener(m_handler, ServiceContext.ServerUp);
        startsWith.add("ht.");
        return null;
    }

    /**
     * Allow a non ht.* package to be included in the unit test process.
     *
     * @param path
     */
    public void addRootPackageForUnitTestScan(String path) {
        startsWith.add(path);
    }

    /**
     * test to see if the path provided is within our test paths
     *
     * @param pathToTest
     * @return
     */
    public boolean isPathWithinTestPaths(String pathToTest) {
        boolean runIt = false;
        for (String path : startsWith) {
            if (pathToTest.startsWith(path)) {
                runIt = true;
                break;
            }
        }
        return runIt;
    }

    public String[] getStartsWith() {
        return startsWith.toArray(new String[startsWith.size()]);
    }

    public String start(boolean dbInit) {
        return null;
    }

    public String deInit() {
        return null;
    }
}

