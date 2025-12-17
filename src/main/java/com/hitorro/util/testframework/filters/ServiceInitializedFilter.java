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
import com.hitorro.util.core.ArrayUtil;
import com.hitorro.util.core.HTAssert;
import com.hitorro.util.core.opers.HTPredicate;
import com.hitorro.util.io.StoreException;
import com.hitorro.util.startupframework.ServiceContext;
import com.hitorro.util.testframework.TestCaseWrapper;
import com.hitorro.util.typesystem.HTObjectInputStream;
import com.hitorro.util.typesystem.HTObjectOutputStream;

import java.io.IOException;

/**
 * CEnsure that a test has all the services it requires.
 */
public class ServiceInitializedFilter implements HTPredicate<TestCaseWrapper> {

    public ServiceInitializedFilter() {

    }

    public boolean initFromMap(final JsonNode map) {
        // MUST IMPLEMENT
        HTAssert.assertThat(false, "ServiceInitialiedFilter.initFromMap not implemented");
        return false;
    }

    public void initForPass() {

    }

    public boolean test(TestCaseWrapper testcase) {
        Class s[] = testcase.testDef.dependentServices();
        if (ArrayUtil.nullOrEmpty(s)) {
            return true;
        }
        for (Class c : s) {
            if (ServiceContext.getSC().getInitializedModule(c) == null) {
                return false;
            }
        }

        return true;
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