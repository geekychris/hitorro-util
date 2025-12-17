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

import com.hitorro.util.core.classes.ClassAnoUtil;
import com.hitorro.util.core.classes.MatchClass;
import com.hitorro.util.core.string.Fmt;

/**
 *
 */
public class TestCaseWrapper {
    public static final MatchClass mc = new MatchClass(HTTest.class);
    public HTTest testDef;
    public Class testClass;

    private TestCaseWrapper(Class c, HTTest test) {
        testDef = test;
        testClass = c;
    }

    public TestCaseWrapper(Class c) {
        testDef = (HTTest) ClassAnoUtil.getClassLevelAnnotation(c, mc);
        testClass = c;
    }

    public static TestCaseWrapper getWrapperIfAppropriate(String s) {
        try {
            return getWrapperIfAppropriate(Class.forName(s));
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    public static TestCaseWrapper getWrapperIfAppropriate(Class c) {
        if (HTTestRunnable.class.isAssignableFrom(c) && c != HTTestRunnable.class) {
            return new TestCaseWrapper(c);
        }

        HTTest testDef = (HTTest) ClassAnoUtil.getClassLevelAnnotation(c, mc);
        if (testDef != null) {
            return new TestCaseWrapper(c, testDef);
        }

        return null;
    }

    public String toString() {
        return Fmt.S("class:%s level: %s", testClass.getCanonicalName(), testDef.runlevel().getName());
    }

    public boolean equals(Object o) {
        if (o instanceof TestCaseWrapper) {
            return ((TestCaseWrapper) o).testClass == testClass;
        }
        return false;
    }

    public int hashCode() {
        return testClass.hashCode();
    }
}
