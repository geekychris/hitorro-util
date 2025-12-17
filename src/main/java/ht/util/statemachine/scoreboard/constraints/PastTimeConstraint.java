package ht.util.statemachine.scoreboard.constraints;

import com.fasterxml.jackson.databind.JsonNode;
import ht.util.core.HTAssert;
import ht.util.core.opers.HTPredicate;
import ht.util.statemachine.scoreboard.StateRow;

/**
 * Copyright (c) 2003 - present HiTorro All rights reserved. User: chris Date: Feb 11, 2007 Time: 6:47:22 PM
 * <p/>
 * If a row is older than the provided time then the constraint passes. We can set times in the past for look for rows
 * that are pased due.
 */
public class PastTimeConstraint implements HTPredicate<StateRow> {
    private long m_time;

    public PastTimeConstraint(long time) {
        m_time = time;
    }

    public boolean initFromMap(final JsonNode map) {
        // MUST IMPLEMENT
        HTAssert.assertThat(false, "PastTimeConstraint.initFromMap not implemented");
        return false;
    }

    public void setTime(long time) {
        m_time = time;
    }

    public boolean test(StateRow stateRow) {
        return stateRow.getLastTouchedTime() < m_time;
    }

    public void initForPass() {

    }
}
