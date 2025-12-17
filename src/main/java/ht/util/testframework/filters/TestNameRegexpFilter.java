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