package ht.util.testframework.filters;

import com.fasterxml.jackson.databind.JsonNode;
import ht.util.core.HTAssert;
import ht.util.core.opers.HTPredicate;
import ht.util.core.string.StringUtil;
import ht.util.testframework.TestCaseWrapper;

/**
 * Look for a package starting with the provided root.  This mechanism ignores case.
 */
public class PackagePrefixFilter implements HTPredicate<TestCaseWrapper> {
    private String m_packagePrefix;

    public PackagePrefixFilter(String root) {
        m_packagePrefix = root;
    }

    public boolean initFromMap(final JsonNode map) {
        // MUST IMPLEMENT
        HTAssert.assertThat(false, "PackagePrefixFilter.initFromMap not implemented");
        return false;
    }

    public void initForPass() {

    }

    public boolean test(TestCaseWrapper testcase) {
        boolean match = false;

        String canon = testcase.testClass.getCanonicalName();

        if (StringUtil.nullOrEmptyOrBlankString(m_packagePrefix)) {
            match = false;
        } else if (StringUtil.startsWithIgnoreCase(canon, m_packagePrefix)) {
            match = true;

        }

        return match;
    }
}