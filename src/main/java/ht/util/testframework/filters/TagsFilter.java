/**
 * Copyright (c) 2003-2008 HiTorro.net
 * <p>
 * <p>
 * description:  junit testcase filter for filtering on EnhancedTestCase.getTags().
 * <p>
 * User: chris
 */

package ht.util.testframework.filters;


import com.fasterxml.jackson.databind.JsonNode;
import ht.util.core.ArrayUtil;
import ht.util.core.HTAssert;
import ht.util.core.opers.HTPredicate;
import ht.util.core.string.StringUtil;
import ht.util.testframework.TestCaseWrapper;


public class TagsFilter implements HTPredicate<TestCaseWrapper> {


    private String m_targetTag;


    public TagsFilter(String tag) {
        setTargetTag(tag);
    }


    public void initForPass() {

    }

    public boolean initFromMap(final JsonNode map) {
        // MUST IMPLEMENT
        HTAssert.assertThat(false, "TagsFilter.initFromMap not implemented");
        return false;
    }

    public boolean test(TestCaseWrapper testcase) {
        boolean match = false;

        String testcaseTags[] = testcase.testDef.tags();

        if (StringUtil.nullOrEmptyOrBlankString(m_targetTag)) {
            match = true;
        } else if (ArrayUtil.getFirstIndexInStringArray(testcaseTags, m_targetTag, false) >= 0) {
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
}