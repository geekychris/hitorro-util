package ht.util.statemachine.csvconsumers;

import ht.util.core.string.StringUtil;
import ht.util.statemachine.MooreStateMachine;

import java.io.File;

/**
 * <p/>
 * Copyright (c) 2003 - present HiTorro All rights reserved. User: chris Date: Feb 3, 2008 Time: 5:59:58 PM
 */
public class ParametersConsumer extends BaseConsumer {

    public static final String ParameterName = "ParameterName";
    public static final String ParameterValue = "ParameterValue";
    public static final String Description = "Description";

    private MooreStateMachine m_registry = null;


    public ParametersConsumer(MooreStateMachine registry, File file) {
        super(registry);

        m_registry = registry;
    }


    protected void processRow(String[] line) {
        String key = getField(ParameterName, line);
        String value = getField(ParameterValue, line);

        if (!StringUtil.nullOrEmptyOrBlankString(key)) {
            m_registry.setProperty(key, value);
        }

    }
}
