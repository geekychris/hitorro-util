package ht.util.core.opers;

import com.fasterxml.jackson.databind.JsonNode;
import ht.util.core.HTAssert;

/**
 * Copyright (c) 2003 - present HiTorro All rights reserved.
 * <p/>
 * User: chris
 */
public class StringContainsOperator implements HTPredicate<String> {
    private boolean m_ignoreCase;
    private String m_term;

    public StringContainsOperator(String term, boolean ignoreCase) {
        m_ignoreCase = ignoreCase;
        m_term = term;
        if (m_ignoreCase) {
            m_term = m_term.toLowerCase();
        }
    }

    public boolean initFromMap(final JsonNode map) {
        // MUST IMPLEMENT
        HTAssert.assertThat(false, "StringContainsOperator.initFromMap not implemented");
        return false;
    }

    public void initForPass() {

    }

    public boolean test(String s) {
        if (m_ignoreCase) {
            // this is expensive!
            s = s.toLowerCase();

        }
        return s.indexOf(m_term) != -1;
    }
}
