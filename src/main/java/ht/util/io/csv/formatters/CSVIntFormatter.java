package ht.util.io.csv.formatters;

/**
 * Copyright (c) 2003 - present HiTorro All rights reserved. User: chris Date: Jan 16, 2008 Time: 7:30:49 AM
 */
public class CSVIntFormatter implements CSVFormatter {
    public String format(int value) {
        return Integer.toString(value);
    }


    public FormatterType getType() {
        return FormatterType.Int;
    }
}
