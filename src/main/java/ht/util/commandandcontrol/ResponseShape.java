package ht.util.commandandcontrol;

import ht.util.commandandcontrol.ano.ColumnGroup;
import ht.util.commandandcontrol.ano.RespColumn;
import ht.util.commandandcontrol.ano.ResponseDefinition;
import ht.util.core.Log;

/**
 * <p/>
 * Copyright (c) 2003 - present HiTorro All rights reserved. User: chris Date: Aug 3, 2005 Time: 8:41:59 AM
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
