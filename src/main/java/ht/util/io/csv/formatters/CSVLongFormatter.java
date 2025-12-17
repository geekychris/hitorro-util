package ht.util.io.csv.formatters;

/**
 * Copyright (c) 2003 - present HiTorro All rights reserved. User: chris Date: Jan 16, 2008 Time: 7:31:30 AM
 */
public class CSVLongFormatter implements CSVFormatter {
    public String format(long value) {
        return Long.toString(value);
    }

    public FormatterType getType() {
        return FormatterType.Long;
    }
}