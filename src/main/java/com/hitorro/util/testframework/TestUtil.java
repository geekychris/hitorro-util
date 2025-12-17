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

import com.fasterxml.jackson.databind.JsonNode;
import com.hitorro.util.commandandcontrol.JUnitListener;
import com.hitorro.util.commandandcontrol.Response;
import com.hitorro.util.core.ListUtil;
import com.hitorro.util.core.Log;
import com.hitorro.util.core.opers.HTPredicate;
import com.hitorro.util.core.opers.LogicalAndOperator;
import com.hitorro.util.core.opers.LogicalOrOperator;
import com.hitorro.util.core.string.Fmt;
import com.hitorro.util.core.string.StringUtil;
import com.hitorro.util.json.keys.BooleanProperty;
import com.hitorro.util.json.keys.IntegerProperty;
import com.hitorro.util.json.keys.StringListFromDelimitedKey;
import com.hitorro.util.json.keys.StringProperty;
import com.hitorro.util.testframework.filters.*;
import junit.framework.JUnit4TestAdapter;
import junit.framework.TestResult;

import java.util.ArrayList;
import java.util.List;

/**
 * Test mechanism that deals with running a test or a suite of tests.
 */
public class TestUtil {
    public static StringProperty Level = new StringProperty("level", "unit test run-level", "smoke");
    public static StringListFromDelimitedKey Test = new StringListFromDelimitedKey("test", "specific unit test or tests that test a regexp string", ",", null);
    public static StringListFromDelimitedKey Tags = new StringListFromDelimitedKey("tags", "filter unit tests via string tags", ",", null);
    public static StringListFromDelimitedKey Packages = new StringListFromDelimitedKey("package", "listFiles of packages that are to be included", ",", null);
    public static StringListFromDelimitedKey Services = new StringListFromDelimitedKey("service", "listFiles of services that tests must be dependent on", ",", null);
    public static StringListFromDelimitedKey Modules = new StringListFromDelimitedKey("module", "listFiles of modules to be tested", ",", null);
    public static BooleanProperty IgnoreTestDependencies = new BooleanProperty("ignoretestdepends", "ignore the dependencies that a states it needs", false);

    public static BooleanProperty Shutdown = new BooleanProperty("shutdown", "shutdown after finished", false);

    public static BooleanProperty EnableService = new BooleanProperty("test.enableservice", "shutdown after finished", false);

    public static IntegerProperty WatchdogSeconds = new IntegerProperty("watchdogseconds", "How long the watchdog will wait", 5 * 60);

    public static TestWatchdogTimer timer = new TestWatchdogTimer();

    public static int runTest(final String test, final JsonNode map, final Response nestedResponse) throws TestException {
        //TestSuite suite = null;
        List<JUnit4TestAdapter> suites = new ArrayList();
        int exitCode = 0;
        if (StringUtil.nullOrEmptyOrBlankString(test)) {
            LogicalAndOperator<TestCaseWrapper> oper = getOperator(map);

            //suite = FilteredTestSuiteGenerator.getTestSuite(oper, null, IgnoreTestDependencies.apply(apply));
            suites = FilteredTestSuiteGenerator.getTestSuiteJ4(oper, null, IgnoreTestDependencies.apply(map));
        } else {
            TestCaseWrapper tcw = TestCaseWrapper.getWrapperIfAppropriate(test);
            if (tcw != null) {
                suites.add(new JUnit4TestAdapter(tcw.testClass));
            } else {
                throw new TestException(Fmt.S("Unable to find class %s or it not a subclass of %s",
                        test, EnhancedTestCase.class.getCanonicalName()));
            }
        }
        JUnitListener l = new JUnitListener(nestedResponse, true);
        if (Shutdown.apply(map)) {
            // we have 5 minutes a test before we quit!
            timer.setTimeout(WatchdogSeconds.apply(map));
            timer.setResponse(nestedResponse, l);
            timer.start();
        }
        /*  run test-suite  */
        if (ListUtil.notNullAndContainsRows(suites)) {
            TestResult result = new TestResult();
            //suite.setName("Test Suite");

            result.addListener(l);
            for (JUnit4TestAdapter t : suites) {
                Log.test.info("Running testsuite %s", t);
                t.run(result);
            }

            nestedResponse.end();

            if (l.getErrorCount() > 0) {
                exitCode = 1;
            }
        }
        timer.stop();
        return exitCode;
    }

    public static LogicalAndOperator<TestCaseWrapper> getOperator(final JsonNode map) {
        LogicalAndOperator<TestCaseWrapper> oper = new LogicalAndOperator<TestCaseWrapper>();

        oper.add(new com.hitorro.util.testframework.filters.RunLevelFilter(RunLevel.getFilterByName(Level.apply(map))));
        oper.add(new com.hitorro.util.testframework.filters.ServiceInitializedFilter());
        oper.addIfNotNull(getPackageConstraint(Packages.apply(map)));
        oper.addIfNotNull(getServiceConstraint(Services.apply(map)));
        oper.addIfNotNull(getModuleConstraint(Modules.apply(map)));
        oper.addIfNotNull(getTestConstraint(Test.apply(map)));
        // or listFiles of
        oper.addIfNotNull(getTagList(Tags.apply(map)));
        oper.finalizeFilter();
        return oper;
    }

    public static final HTPredicate<TestCaseWrapper> getPackageConstraint(List<String> pack) {
        if (ListUtil.nullOrEmpty(pack)) {
            return null;
        }
        LogicalOrOperator<TestCaseWrapper> oper = new LogicalOrOperator<TestCaseWrapper>();
        for (String p : pack) {
            oper.add(new com.hitorro.util.testframework.filters.PackagePrefixFilter(p));
        }
        oper.finalizeFilter();
        return oper;

    }

    public static final HTPredicate<TestCaseWrapper> getServiceConstraint(List<String> pack) {
        if (ListUtil.nullOrEmpty(pack)) {
            return null;
        }
        LogicalOrOperator<TestCaseWrapper> oper = new LogicalOrOperator<TestCaseWrapper>();
        for (String p : pack) {
            oper.add(new com.hitorro.util.testframework.filters.ServiceFilter(p));
        }
        oper.finalizeFilter();
        return oper;

    }

    public static final HTPredicate<TestCaseWrapper> getModuleConstraint(List<String> pack) {
        if (ListUtil.nullOrEmpty(pack)) {
            return null;
        }
        LogicalOrOperator<TestCaseWrapper> oper = new LogicalOrOperator<TestCaseWrapper>();
        for (String p : pack) {
            oper.add(new com.hitorro.util.testframework.filters.ModuleFilter(p));
        }
        oper.finalizeFilter();
        return oper;

    }

    public static final HTPredicate<TestCaseWrapper> getTagList(List<String> tags) {
        if (ListUtil.nullOrEmpty(tags)) {
            return null;
        }
        LogicalOrOperator<TestCaseWrapper> oper = new LogicalOrOperator<TestCaseWrapper>();
        for (String p : tags) {
            oper.add(new com.hitorro.util.testframework.filters.TagsFilter(p));
        }
        oper.finalizeFilter();
        return oper;
    }

    public static final HTPredicate<TestCaseWrapper> getTestConstraint(List<String> tests) {
        if (ListUtil.nullOrEmpty(tests)) {
            return null;
        }
        LogicalOrOperator<TestCaseWrapper> oper = new LogicalOrOperator<TestCaseWrapper>();
        for (String p : tests) {
            oper.add(new com.hitorro.util.testframework.filters.TestNameRegexpFilter(p));
        }
        oper.finalizeFilter();
        return oper;
    }
}
