package ht.util.io.csv;

import gnu.trove.map.hash.TObjectIntHashMap;
import ht.util.core.Constants;

/**
 * Copyright (c) 2003 - present HiTorro All rights reserved. User: chris Date: Feb 9, 2005 Time: 10:02:25 AM
 * <p/>
 * * Represents a wrapper of column name to index position into the a table. This is used so that column names can be
 * translated to index positions and from index positions to the content of a row of the table.
 */

public class ColumnTableMeta {
    private TObjectIntHashMap<String> m_columns = new TObjectIntHashMap<String>();
    private String[] columnNames;

    private ColumnTableMeta() {
        // Dont want anyone to init this way
    }

    public static ColumnTableMeta init(String[] line) {
        ColumnTableMeta result = new ColumnTableMeta();
        result.setColumnNames(line);
        if (line != null) {
            int length = line.length;
            for (int i = 0; i < length; i++) {
                String name = line[i];
                result.addColumn(name, Constants.getInteger(i));
            }
        }

        return result;
    }

    public int getSize() {
        return columnNames.length;
    }

    public String[] getColumnNames() {
        return columnNames;
    }

    private void setColumnNames(String line[]) {
        columnNames = line;
    }

    private void addColumn(String columnName, Integer index) {
        m_columns.put(columnName.trim(), index);
    }

    public int getColumnInteger(String columnName) {
        return m_columns.get(columnName);
    }

    public String get(String name, String row[]) {
        return row[getColumnInteger(name)];
    }

    public int getColumnInt(String columnName) {
        Integer i = getColumnInteger(columnName.trim());
        if (i != null) {
            return i.intValue();
        }
        return -1;
    }
}
