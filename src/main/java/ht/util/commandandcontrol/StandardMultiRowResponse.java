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
package ht.util.commandandcontrol;

import java.util.LinkedList;
import java.util.Queue;

/**
 * <p/>
 * Copyright (c) 2003 - present HiTorro All rights reserved. User: chris Date: Aug 3, 2005 Time: 8:53:08 AM
 */
public class StandardMultiRowResponse extends MultiRowResponse {
    private Queue<Object>[] m_columns;

    @SuppressWarnings("unchecked")
    StandardMultiRowResponse(int columns, ResponseShape r) {
        super(columns, r);
        m_columns = new Queue[columns];
        for (int i = 0; i < columns; i++) {
            m_columns[i] = new LinkedList<Object>();
        }
        if (shape.m_group == null) {
            shape.m_group = new GroupTuple[columns];
        }
    }

    public void addTupleArray(int offset, Object elems[]) {
        for (int i = 0; i < elems.length; i++) {
            add(offset + i, elems[i]);
        }
    }

    public void addThrowable(int column, Throwable t, int stackDepth, int startFrom) {
        if (t == null) {
            return;
        }
        StackTraceElement[] elements = t.getStackTrace();
        stackDepth = Math.min(stackDepth + startFrom, elements.length);
        for (int i = startFrom; i < stackDepth; i++) {
            add(column, elements[i].toString());
        }
    }

    public void clear() {
        for (Queue<Object> column : m_columns) {
            column.clear();
        }
    }

    public boolean add(int index, Object addMe) {
        if (index < m_columns.length) {
            m_columns[index].add(addMe);
            return true;
        }
        return false;
    }

    /**
     * Process the contents of this multi row response into a normal response object.
     *
     * @param response
     */
    public void addToResponse(Response response) {
        boolean added = true;
        Object[] frame = new Object[m_columns.length];
        while (added == true) {
            added = false;
            for (int i = 0; i < m_columns.length; i++) {
                Queue<Object> column = m_columns[i];
                if (column.size() > 0) {
                    added = true;
                    frame[i] = column.remove();
                } else {
                    frame[i] = "";
                }
            }
            // finally put the row to the output.
            response.setRenderingRow(this.containers);
            response.addRow(frame);
        }
    }
}
