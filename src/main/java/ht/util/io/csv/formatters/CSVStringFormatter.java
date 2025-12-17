package ht.util.io.csv.formatters;

/**
 * Copyright (c) 2003 - present HiTorro All rights reserved. User: chris Date: Jan 16, 2008 Time: 7:44:30 AM
 */

public class CSVStringFormatter implements CSVFormatter {
    public String format(String value) {
        return value;
    }

    public FormatterType getType() {
        return FormatterType.String;
    }
}