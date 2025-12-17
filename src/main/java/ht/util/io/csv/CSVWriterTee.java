package ht.util.io.csv;

import java.util.List;

/**
 * Copyright (c) 2003 - present HiTorro All rights reserved. User: chris Date: Jan 16, 2008 Time: 11:16:09 AM
 */
public class CSVWriterTee implements CSVWriter {
    private CSVWriterTee left;
    private CSVWriterTee right;

    public CSVWriterTee(CSVWriterTee left, CSVWriterTee right) {
        this.left = left;
        this.right = right;
    }

    public void writeRow(Object values[]) {
        left.writeRow(values);
        right.writeRow(values);
    }

    public void writeRow(List<String> values) {
        left.writeRow(values);
        right.writeRow(values);
    }

    public void close() {
        left.close();
        right.close();
    }
}
