package ht.util.statemachine.csvconsumers;

import ht.util.statemachine.MooreStateMachine;

/**
 * Copyright (c) 2003 - present HiTorro All rights reserved. User: chris Date: Feb 9, 2007 Time: 8:45:57 AM
 */
public class EdgeConsumer extends BaseConsumer {
    public static final String StateName = "StateName";
    public static final String NextState = "NextState";
    public static final String Validator = "Validator";
    public static final String Description = "Description";
    public static final String UIChoice = "UIChoice";
    public static final String ValidatorParameters = "ValidatorParameters";

    public EdgeConsumer(MooreStateMachine registry) {
        super(registry);
    }

    protected void processRow(String[] line) {
        m_registry.addStateTransition(getField(StateName, line),
                getField(NextState, line),
                getField(Description, line),
                getField(Validator, line),
                getField(UIChoice, line),
                getField(ValidatorParameters, line));
    }
}