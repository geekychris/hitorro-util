package ht.util.testframework;

import ht.util.core.classes.ClassAnoUtil;
import ht.util.core.classes.MatchClass;
import ht.util.core.string.Fmt;

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
