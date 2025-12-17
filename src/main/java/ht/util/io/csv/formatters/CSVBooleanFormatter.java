package ht.util.io.csv.formatters;

import ht.util.core.BooleanUtil;

/**
 * Copyright (c) 2003 - present HiTorro All rights reserved. User: chris Date: Jan 16, 2008 Time: 7:32:00 AM
 */
public class CSVBooleanFormatter implements CSVFormatter {
    public String format(boolean value) {
        return BooleanUtil.getTrueFalseFlag(value);
    }

    public FormatterType getType() {
        return FormatterType.Boolean;
    }
}