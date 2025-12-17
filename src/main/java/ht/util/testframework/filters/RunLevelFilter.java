/**
 * Copyright (c) 2003-2008 HiTorro.net
 * <p>
 * <p>
 * description:  junit testcase filter for filtering upon EnhancedTestCase.getRunLevel().
 * typically filters _in_ on the runtime command-line parameter: level.
 * <p>
 * User: chris
 */

package ht.util.testframework.filters;

import com.fasterxml.jackson.databind.JsonNode;
import ht.util.core.HTAssert;
import ht.util.core.opers.HTPredicate;
import ht.util.testframework.FilteredTestSuiteGenerator;
import ht.util.testframework.RunLevel;
import ht.util.testframework.TestCaseWrapper;

public class RunLevelFilter implements HTPredicate<TestCaseWrapper> {

    RunLevel m_suiteLevel = RunLevel.Never;


    public RunLevelFilter(RunLevel suiteLevel) {

        if (suiteLevel != null) {
            setSuiteRunLevel(suiteLevel);
        } else {
            setSuiteRunLevel(FilteredTestSuiteGenerator.getSuiteRunLevel());
        }
    }

    public boolean initFromMap(final JsonNode map) {
        // MUST IMPLEMENT
        HTAssert.assertThat(false, "RunLevelFilter.initFromMap not implemented");
        return false;
    }

    public void initForPass() {
    }

    public boolean test(TestCaseWrapper t) {
        boolean match = false;

        RunLevel testcaseLevel = t.testDef.runlevel();

        if (testcaseLevel != RunLevel.Never) {
            if (m_suiteLevel.ordinal() >= testcaseLevel.ordinal()) {
                match = true;
            }
        }

        return match;
    }


    public RunLevel getSuiteRunLevel() {
        return m_suiteLevel;
    }


    public void setSuiteRunLevel(RunLevel suiteLevel) {
        m_suiteLevel = suiteLevel;
    }
}
