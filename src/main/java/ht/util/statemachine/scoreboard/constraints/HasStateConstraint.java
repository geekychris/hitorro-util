package ht.util.statemachine.scoreboard.constraints;

import com.fasterxml.jackson.databind.JsonNode;
import ht.util.core.HTAssert;
import ht.util.core.opers.HTPredicate;
import ht.util.statemachine.State;
import ht.util.statemachine.scoreboard.StateRow;

/**
 * Copyright (c) 2003 - present HiTorro All rights reserved. User: chris Date: Feb 11, 2007 Time: 6:51:07 PM
 * <p/>
 * Match rows that have a given state.
 */
public class HasStateConstraint implements HTPredicate<StateRow> {
    private State m_state;

    public HasStateConstraint(State state) {
        m_state = state;
    }

    public boolean initFromMap(final JsonNode map) {
        // MUST IMPLEMENT
        HTAssert.assertThat(false, "HasStateConstraint.initFromMap not implemented");
        return false;
    }

    public boolean test(StateRow stateRow) {
        return stateRow.getState().equals(m_state);
    }

    public void initForPass() {

    }
}
