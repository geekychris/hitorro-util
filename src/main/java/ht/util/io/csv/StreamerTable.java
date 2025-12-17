package ht.util.io.csv;

/**
 * Copyright (c) 2003 - present HiTorro All rights reserved. User: chris Date: Feb 9, 2005 Time: 10:02:10 AM
 */
public interface StreamerTable {
    ColumnTableMeta getMeta();

    void setMeta(ColumnTableMeta meta);

    String[] getRow();

    void setRow(String[] row);
}