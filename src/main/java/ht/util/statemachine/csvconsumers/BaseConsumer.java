package ht.util.statemachine.csvconsumers;

import ht.util.core.string.StringUtil;
import ht.util.io.csv.ColumnTableMeta;
import ht.util.io.csv.csvconsumer.CSVConsumer;
import ht.util.statemachine.MooreStateMachine;


/**
 * Copyright (c) 2003 - present HiTorro All rights reserved. User: chris Date: Feb 9, 2007 Time: 8:47:12 AM
 * <p/>
 * base class that one subclasses to load state, group and edge data into a state machine
 */
public abstract class BaseConsumer implements CSVConsumer {
    protected MooreStateMachine m_registry;
    protected ColumnTableMeta m_meta;
    private boolean m_firstRowProcessed = false;

    public BaseConsumer(MooreStateMachine registry) {
        m_registry = registry;
    }

    public void line(int row, String[] line) {
        if (!m_firstRowProcessed) {
            m_meta = ColumnTableMeta.init(line);
            m_firstRowProcessed = true;
        } else {
            processRow(line);
        }
    }

    protected abstract void processRow(String[] line);

    protected String getField(String name, String[] line) {
        int i = m_meta.getColumnInt(name);
        if (i == -1) {
            return null;
        }

        if (i >= line.length) {
            // out of range, too large
            return null;
        }
        String returnVal = line[i];
        if (StringUtil.nullOrEmptyOrBlankString(returnVal)) {
            return null;
        }
        return returnVal.trim();
    }
}
