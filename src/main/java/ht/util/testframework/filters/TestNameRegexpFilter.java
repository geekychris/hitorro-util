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
package ht.util.testframework.filters;

import com.fasterxml.jackson.databind.JsonNode;
import ht.util.core.HTAssert;
import ht.util.core.opers.HTPredicate;
import ht.util.core.string.StringUtil;
import ht.util.io.StoreException;
import ht.util.testframework.TestCaseWrapper;
import ht.util.typesystem.HTObjectInputStream;
import ht.util.typesystem.HTObjectOutputStream;

import java.io.IOException;


/**
 * RegExp based test name filter.
 * <p/>
 * http://java.sun.com/j2se/1.4.2/docs/api/java/util/regex/Pattern.html#sum
 * <p/>
 * Copyright (c) 2003 - present HiTorro All rights reserved. User: chris
 */

public class TestNameRegexpFilter implements HTPredicate<TestCaseWrapper> {
    private String m_targetTag;

    public TestNameRegexpFilter(String tag) {
        setTargetTag(tag);
    }

    public boolean initFromMap(final JsonNode map) {
        // MUST IMPLEMENT
        HTAssert.assertThat(false, "TestNameRegexpFilter.initFromMap not implemented");
        return false;
    }

    public void initForPass() {

    }

    public boolean test(TestCaseWrapper testcase) {
        boolean match = false;

        String name = testcase.testClass.getCanonicalName();

        if (StringUtil.nullOrEmptyOrBlankString(m_targetTag)) {
            match = true;
        } else if (name.matches(m_targetTag)) {
            match = true;
        }

        return match;
    }


    public String getTag() {
        return m_targetTag;
    }


    public void setTargetTag(String tag) {
        m_targetTag = tag;
    }

    public void serialize(HTObjectOutputStream os) throws IOException, StoreException {

    }

    public void deserialize(HTObjectInputStream os) throws IOException, ClassNotFoundException, StoreException {
    }

    public int getSerializationVersion() {
        return 0;
    }

    public boolean isPersisted() {
        return false;
    }

    public boolean hasGuid() {
        return false;
    }

    public boolean hasSoftGuid() {
        return false;
    }
}