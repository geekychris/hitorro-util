package ht.util.statemachine.csvconsumers;

import ht.util.statemachine.MooreStateMachine;

/**
 * Copyright (c) 2003 - present HiTorro All rights reserved. User: chris Date: Feb 9, 2007 Time: 8:44:42 AM
 */
public class StateConsumer extends BaseConsumer {
    public static final String StateName = "StateName";
    public static final String Retries = "Retries";
    public static final String Group = "Group";
    public static final String Action = "Action";
    public static final String Description = "Description";
    public static final String RecoveryState = "RecoveryState";
    public static final String ActionParameters = "ActionParameters";

    public StateConsumer(MooreStateMachine registry) {
        super(registry);
    }

    protected void processRow(String[] line) {
        m_registry.addState(getField(StateName, line),
                getField(Group, line),
                getField(Description, line),
                getField(Action, line),
                getField(Retries, line),
                getField(RecoveryState, line),
                getField(ActionParameters, line));
    }
}