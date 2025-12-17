package ht.util.excelaccess;

import ht.util.core.string.Fmt;


/**
 * Copyright (c) 2003 - present HiTorro All rights reserved. User: chris Date: Jan 31, 2005 Time: 10:46:06 AM
 */
public class DetailedSpreadSheetException extends RuntimeException {
    private String m_description;
    private int m_sheetNum;
    private int m_row;
    private int m_column;

    public DetailedSpreadSheetException(Exception description, int sheetNum, int row, int column) {
        super(Fmt.S("%s sheet: %s row: %s column: %s", description.toString(), Integer.toString(sheetNum), Integer.toString(row), Integer.toString(column)));
        m_description = description.toString();
        m_sheetNum = sheetNum;
        m_row = row;
        m_column = column;
    }

    public DetailedSpreadSheetException(String description, int sheetNum, int row, int column) {
        super(Fmt.S("%s sheet: %s row: %s column: %s", description, Integer.toString(sheetNum), Integer.toString(row), Integer.toString(column)));
        m_description = description;
        m_sheetNum = sheetNum;
        m_row = row;
        m_column = column;
    }

}
