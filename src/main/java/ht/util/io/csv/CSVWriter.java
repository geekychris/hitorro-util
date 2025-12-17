package ht.util.io.csv;

import java.util.List;

/**
 * Copyright (c) 2003 - present HiTorro All rights reserved. User: chris Date: Jan 16, 2008 Time: 7:55:01 AM
 */
public interface CSVWriter {
    void writeRow(Object values[]);

    void writeRow(List<String> values);

    void close();
}
