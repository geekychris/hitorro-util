package ht.util.io.csv;


/**
 * Copyright (c) 2003-2008 HiTorro.
 * <p/>
 * User: chris Date: Apr 18, 2004 Time: 1:07:20 PM
 * <p/>
 * Description:
 */
public class CSVStreamerTableRow implements StreamerTable {
    private ColumnTableMeta m_meta;
    private String[] m_row;

    public ColumnTableMeta getMeta() {
        return m_meta;
    }

    public void setMeta(ColumnTableMeta meta) {
        m_meta = meta;
    }

    public String[] getRow() {
        return m_row;
    }

    public void setRow(String[] row) {
        m_row = row;
    }
}
