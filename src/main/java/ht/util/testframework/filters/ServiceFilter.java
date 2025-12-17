package ht.util.testframework.filters;

import com.fasterxml.jackson.databind.JsonNode;
import ht.util.core.ArrayUtil;
import ht.util.core.HTAssert;
import ht.util.core.opers.HTPredicate;
import ht.util.testframework.TestCaseWrapper;

/**
 *
 */
public class ServiceFilter implements HTPredicate<TestCaseWrapper> {
    private String name;


    public ServiceFilter(String name) {
        this.name = name;
    }

    public boolean initFromMap(final JsonNode map) {
        // MUST IMPLEMENT
        HTAssert.assertThat(false, "ServiceFilter.initFromMap not implemented");
        return false;
    }

    public void initForPass() {

    }

    public boolean test(TestCaseWrapper testcase) {
        Class s[] = testcase.testDef.dependentServices();
        if (ArrayUtil.nullOrEmpty(s)) {
            return false;
        }
        for (Class c : s) {
            if (c.getCanonicalName().equalsIgnoreCase(name)) {
                return true;
            }
        }

        return false;
    }
}
