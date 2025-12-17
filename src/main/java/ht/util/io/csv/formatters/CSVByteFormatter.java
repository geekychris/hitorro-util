package ht.util.io.csv.formatters;

/**
 * Copyright (c) 2003 - present HiTorro All rights reserved. User: chris Date: Jan 16, 2008 Time: 8:11:58 AM To change this
 * template use File | Settings | File Templates.
 */

public class CSVByteFormatter implements CSVFormatter {
    public String format(byte value) {
        return Byte.toString(value);
    }

    public FormatterType getType() {
        return FormatterType.Byte;
    }
}
