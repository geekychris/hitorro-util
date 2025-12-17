package ht.util.io.csv.formatters;

import java.util.Date;

/**
 * Copyright (c) 2003 - present HiTorro All rights reserved. User: chris Date: Jan 16, 2008 Time: 7:32:34 AM
 */
public class CSVDateFormatter implements CSVFormatter {
    public String format(Date value) {
        return value.toString();
    }

    public FormatterType getType() {
        return FormatterType.Date;
    }
}