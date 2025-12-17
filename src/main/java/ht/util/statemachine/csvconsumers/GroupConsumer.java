package ht.util.statemachine.csvconsumers;

import ht.util.statemachine.MooreStateMachine;

/**
 * Copyright (c) 2003 - present HiTorro All rights reserved. User: chris Date: Feb 9, 2007 Time: 8:44:17 AM
 */
public class GroupConsumer extends BaseConsumer {
    public static final String GroupName = "GroupName";
    public static final String ParentGroup = "ParentGroup";
    public static final String Description = "Description";

    public GroupConsumer(MooreStateMachine registry) {
        super(registry);
    }

    protected void processRow(String[] line) {
        m_registry.addGroup(getField(GroupName, line),
                getField(ParentGroup, line),
                getField(Description, line));
    }
}



