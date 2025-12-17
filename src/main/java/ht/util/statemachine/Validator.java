package ht.util.statemachine;

import ht.jsontypesystem.JVS;
import ht.util.core.Constants;
import ht.util.statemachine.scoreboard.StateRow;

/**
 * Copyright (c) 2003 - present HiTorro All rights reserved.
 * Copyright (c) 2003 - present HiTorro All rights reserved.
 * User: chris
 * Date: Feb 9, 2007
 * Time: 8:36:06 AM
 */

/**
 * Description: Validate that we can transition from the current state to the desired state
 */
public abstract class Validator<E> {
    private JVS m_parameters = null;

    public void setParameters(JVS parameters) {
        m_parameters = parameters;
    }

    public abstract boolean validate(E elem, DirectedEdge edge, State nextState);

    protected boolean logTrue(String reason, E elem, State currentState, State nextState) {
        Log.statemachine.debug(
                "Validator %s returned true for %s testing transition %s to %s, reason %s",
                getName(), elem, currentState, nextState, reason);
        return true;
    }

    protected boolean logFalse(String reason, E elem, State currentState, State nextState) {
        Log.statemachine.debug(
                "Validator %s returned false for %s testing transition %s to %s, reason %s",
                getName(), elem, currentState, nextState, reason);
        return false;
    }

    protected String getName() {
        return getClass().getName();
    }

    /**
     * Determine if the current row has "matured", that it has not been touched in at least n seconds.
     *
     * @param elem
     * @return true if matured.
     */
    protected boolean lastTouchedOlderThan(StateRow elem, int secs) {
        long olderThanTime = System.currentTimeMillis() - secs * Constants.MillisInSecond;


        return elem.getLastTouchedTime() < olderThanTime;
    }
}