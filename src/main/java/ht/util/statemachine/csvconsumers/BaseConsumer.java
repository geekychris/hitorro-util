/*
 * Copyright (c) 2006-2025 Chris Collins
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
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
