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
package com.hitorro.util.testframework.filters;

import com.fasterxml.jackson.databind.JsonNode;
import com.hitorro.util.core.HTAssert;
import com.hitorro.util.core.opers.HTPredicate;
import com.hitorro.util.core.string.StringUtil;
import com.hitorro.util.testframework.TestCaseWrapper;

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