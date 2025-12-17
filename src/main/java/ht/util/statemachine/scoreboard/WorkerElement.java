package ht.util.statemachine.scoreboard;

/**
 * Copyright (c) 2003 - present HiTorro All rights reserved. User: chris Date: Feb 11, 2007 Time: 8:04:46 PM
 * <p/>
 * Element that contains the stateRow and the action to perform
 */
public class WorkerElement<P> {
    private StateRowAction<P> m_action;
    private StateRow<P> m_row;

    public WorkerElement(StateRowAction<P> action, StateRow<P> row) {
        m_action = action;
        m_row = row;
    }

    public void execute() {
        m_action.execute(m_row);
    }
}
