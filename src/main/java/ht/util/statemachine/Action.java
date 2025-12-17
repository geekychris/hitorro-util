package ht.util.statemachine;

import ht.jsontypesystem.JVS;

/**
 * Copyright (c) 2003 - present HiTorro All rights reserved. User: chris Date: Feb 8, 2007 Time: 7:57:19 PM
 */
public abstract class Action<E, C> {
    private JVS m_parameters = null;

    public void setParameters(JVS parameters) {
        m_parameters = parameters;
    }

    /**
     * @return true means launch modifier in own thread.
     */
    public abstract boolean runInSeperateThread();

    /**
     * @param elem
     * @param containingStructure, this could be a scoreboard or nothing at all if the elements dont need external
     *                             compContext
     * @return
     * @throws Exception
     */
    public abstract boolean modifyState(E elem, C containingStructure) throws Exception;

    /**
     * Some states when we recover need to go to a previous state (merger with filer for instance)
     *
     * @return
     */
    public State recoveryState() {
        return null;
    }

}
