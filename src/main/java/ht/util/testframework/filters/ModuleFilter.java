package ht.util.testframework.filters;

import com.fasterxml.jackson.databind.JsonNode;
import ht.util.core.HTAssert;
import ht.util.core.opers.HTPredicate;
import ht.util.core.string.StringUtil;
import ht.util.testframework.TestCaseWrapper;

/**
 * Copyright (c) 2003 - present HiTorro All rights reserved.
 */
public class ModuleFilter implements HTPredicate<TestCaseWrapper> {
    private String module;

    public ModuleFilter(String module) {
        this.module = module;
    }

    public boolean initFromMap(final JsonNode map) {
        // MUST IMPLEMENT
        HTAssert.assertThat(false, "ModuleFilter.initFromMap not implemented");
        return false;
    }

    public void initForPass() {
    }

    public boolean test(TestCaseWrapper testcase) {
        String m = testcase.testDef.module();
        if (!StringUtil.nullOrEmptyString(m)) {
            return module.equalsIgnoreCase(m);
        }
        return false;

    }
}
