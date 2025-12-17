package ht.util.excelaccess;

import ht.util.core.Constants;


/**
 * Copyright (c) 2003 - present HiTorro All rights reserved. User: chris Date: Jan 31, 2005 Time: 10:46:06 AM
 */
public class ExcelUtil {
    public static final int Excel5 = 0;
    public static final int Excel97 = 1;

    /**
     * The max number of rows per spread sheet.
     */
    public static int getMaxRowsPerSheet(int excelFormat) {
        switch (excelFormat) {
            case Excel5:
                return 16384;
            case Excel97:
                return 65536;
            default:
                Log.util.error("unrecoganized excel format %s", Constants.getInteger(excelFormat));
                return 0;
        }
    }
}