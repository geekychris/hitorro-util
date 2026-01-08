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

import com.hitorro.util.core.ArrayUtil;
import com.hitorro.util.core.Log;
import com.hitorro.util.core.classes.ClassPathDeepIterator;
import com.hitorro.util.core.classes.ClassUtil;
import com.hitorro.util.core.opers.HTPredicate;
import com.hitorro.util.core.string.Fmt;
import com.hitorro.util.core.string.StringUtil;
import junit.framework.JUnit4TestAdapter;
import junit.framework.TestSuite;

import java.util.*;

/**
 * <p/>
 * Enumerate the classpath and get all the tests that meet a test level criteria to run
 */

public class FilteredTestSuiteGenerator {

    private static RunLevel m_suiteRunLevel = RunLevel.Never;


    /**
     * Grab all the tests, we will throw an error if a test dependency cannot be satisfied unless the
     * ignoreTestDependencies is set to true.
     * <p/>
     * Any test that requires a service that does not exist will be removed.
     *
     * @param oper
     * @param comp
     * @param ignoreTestDependencies
     * @return
     */
    public static final TestSuite getTestSuite(HTPredicate<TestCaseWrapper> oper,
                                               Comparator<TestCaseWrapper> comp,
                                               boolean ignoreTestDependencies) throws TestException {


        TestSuite ts = new TestSuite();
        List<TestCaseWrapper> list = getTestCases(oper, comp, ignoreTestDependencies);

        for (TestCaseWrapper test : list) {

            ts.addTestSuite(test.testClass);
        }

        return ts;
    }

    public static final List<JUnit4TestAdapter> getTestSuiteJ4(HTPredicate<TestCaseWrapper> oper,
                                                               Comparator<TestCaseWrapper> comp,
                                                               boolean ignoreTestDependencies) throws TestException {

        List<JUnit4TestAdapter> l = new ArrayList();
        TestSuite ts = new TestSuite();
        List<TestCaseWrapper> list = getTestCases(oper, comp, ignoreTestDependencies);

        for (TestCaseWrapper test : list) {
            l.add(new JUnit4TestAdapter(test.testClass));
        }
        return l;
    }


    public static final List<TestCaseWrapper> getTestCases(HTPredicate<TestCaseWrapper> oper,
                                                           Comparator<TestCaseWrapper> comp,
                                                           boolean ignoreTestDependencies)
            throws TestException {
        List<TestCaseWrapper> list = getTestCasesAux(oper);
        List<TestCaseWrapper> result = new ArrayList<TestCaseWrapper>();
        Map<Class, TestCaseWrapper> tests = new HashMap<Class, TestCaseWrapper>();
        if (comp != null) {
            Collections.sort(list, comp);
        } else {
            // order by the test dependencies
            Collections.sort(list, new ByTestDependency());
        }
        for (TestCaseWrapper c : list) {
            tests.put(c.getClass(), c);
        }

        for (TestCaseWrapper test : list) {
            if (!ignoreTestDependencies) {
                Class deps[] = test.testDef.dependentTests();
                if (!ArrayUtil.nullOrEmpty(deps)) {
                    for (Class c : deps) {
                        if (!tests.containsKey(c)) {
                            throw new TestException(Fmt.S("Unable to find test %s that %s depends on", c, test.getClass()));
                        }
                    }
                }
            }
            result.add(test);
        }
        return result;
    }

    /**
     * get a listFiles of test cases filtered by the run level and above.
     *
     * @param oper
     * @return
     */
    public static final List<TestCaseWrapper> getTestCasesAux(HTPredicate<TestCaseWrapper> oper) {
        List<TestCaseWrapper> testCases = new ArrayList<TestCaseWrapper>();
        ClassPathDeepIterator iter = new ClassPathDeepIterator();


        Set<TestCaseWrapper> wrapMap = new HashSet();
        while (iter.hasNext()) {
            String orig = iter.next();
            String c = ClassUtil.translateClassFilenameToCanonical(orig);
            if (!StringUtil.nullOrEmptyOrBlankString(c)) {
                if (!TestServerService.getInstance().isPathWithinTestPaths(c)) {
                    continue;
                }
                try {
                    Class cl = Class.forName(c);
                    TestCaseWrapper tcw = TestCaseWrapper.getWrapperIfAppropriate(cl);
                    if (tcw != null && tcw.testDef != null) {
                        if (oper.test(tcw)) {
                            if (wrapMap.contains(tcw)) {
                                  /*
                                     we can get more than one instance of the class due to the fact we can include
                                     jar files and the build path.  This is to ensure we only call the test case once.
                                   */
                                continue;
                            }
                            wrapMap.add(tcw);
                            testCases.add(tcw);
                            Log.test.debug("Added %s test to the listFiles of tests to run", c);
                        }
                    }
                } catch (ClassNotFoundException e) {
                    Log.util.error("Unable to get class for %s %e", e, e);
                } catch (NoClassDefFoundError e) {
                    Log.util.error("Unable to get class for %s %e", e, e);
                }
            }
        }
        return testCases;
    }


    public static final RunLevel getSuiteRunLevel() {
        return m_suiteRunLevel;
    }

}

class ByTestDependency implements Comparator<TestCaseWrapper> {
    public int compare(final TestCaseWrapper a, final TestCaseWrapper b) {
        Class depends[] = a.testDef.dependentTests();
        if (!ArrayUtil.nullOrEmpty(depends)) {
            if (ArrayUtil.contains(depends, b.getClass())) {
                // a depends on b
                return 1;
            }
        }
        depends = a.testDef.dependentTests();
        if (!ArrayUtil.nullOrEmpty(depends)) {
            if (ArrayUtil.contains(depends, a.getClass())) {
                // b depends on a
                return -1;
            }
        }
        return 0;
    }
}


