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
package com.hitorro.util.commandandcontrol;

import com.hitorro.util.commandandcontrol.ano.ColumnGroup;
import com.hitorro.util.commandandcontrol.ano.RespColumn;
import com.hitorro.util.commandandcontrol.ano.ResponseDefinition;
import com.hitorro.util.core.Log;

/**
 * <p/>
 */
public class ResponseShape {
    protected ResponseRow m_header;
    protected String m_headerShort[];
    protected Class m_types[];

    protected GroupTuple m_group[];

    protected String transaction;
    protected String rowName;

    public ResponseShape() {

    }

    public ResponseShape(String trans, String row) {
        transaction = trans;
        rowName = row;
    }

    public void setTransaction(String trans) {
        transaction = trans;
    }

    public ResponseShape getCopy() {
        ResponseShape rs = new ResponseShape();
        rs.m_group = m_group;
        rs.m_header = m_header;
        rs.m_headerShort = m_headerShort;
        rs.m_types = m_types;
        rs.transaction = transaction;
        rs.rowName = rowName;
        return rs;
    }

    public void setFromDef(ResponseDefinition def) {
        if (def == null) {
            Log.commands.fatal("Definition was null");
            return;
        }
        int size = def.columns().length;
        String header[] = new String[size];
        String shortNames[] = new String[size];
        Class types[] = new Class[size];
        for (int i = 0; i < size; i++) {
            RespColumn rc = def.columns()[i];
            header[i] = rc.lName();
            shortNames[i] = rc.name();
            types[i] = rc.type();
        }

        addHeaderArray(header);
        addRowTypesArray(types);
        addHeaderShortNamesArray(shortNames);
        transaction = def.command();
        rowName = def.rowname();

        for (int i = 0; i < def.groups().length; i++) {
            ColumnGroup cg = def.groups()[i];
            addGroup(cg.name(), cg.name(), cg.name(), cg.groupType(), cg.start(), cg.size());
        }
    }

    public GroupTuple[] getGroups() {
        return m_group;
    }

    public String[] getHeaderShort() {
        return m_headerShort;
    }

    public String[] getHeaderLong() {
        return m_header.getHeader();
    }

    public Class[] getClasses() {
        return m_types;
    }

    public String getRowName() {
        return rowName;
    }

    public void setRowName(String rowName) {
        this.rowName = rowName;
    }

    public String[] getShortNames() {
        return m_headerShort;
    }

    public void addHeader(String... headers) {
        addHeaderArray(headers);
    }

    public void addHeaderArray(String headers[]) {
        m_header = new ResponseRow(headers);
        m_group = new GroupTuple[headers.length];
    }

    public void addHeaderShortNames(String... columnHeaders) {
        addHeaderShortNamesArray(columnHeaders);
    }

    public void addHeaderShortNamesArray(String columnHeaders[]) {
        m_headerShort = columnHeaders;
    }

    public void addRowTypes(Class... types) {
        addRowTypesArray(types);
    }

    public void addRowTypesArray(Class types[]) {
        m_types = types;
    }

    /**
     * Define the columns that belong together in a group.
     *
     * @param groupName
     * @param shortGroupName
     * @param groupType
     */
    public void addGroup(String setName, String groupName, String shortGroupName, Class groupType, int offset, int size) {
        GroupTuple gt = new GroupTuple(setName, groupName, shortGroupName, groupType, size);

        for (int i = 0; i < size; i++) {
            // plaster the group over all its positions.
            m_group[offset + i] = gt;
        }
    }
}
