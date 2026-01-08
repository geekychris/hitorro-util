/*
 * Copyright (c) 2006-2025 Chris Collins
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.hitorro.util.io.csv;

import com.hitorro.util.io.csv.formatters.*;

import java.util.Date;

/**
 */
public class CSVFormattedWriter {
    private com.hitorro.util.io.csv.formatters.CSVFormatter[] formatters;
    private String[] columnNames;
    private Object[] outputRow;

    public CSVFormattedWriter(String columnNames[], com.hitorro.util.io.csv.formatters.CSVFormatter formatters[]) {
        this.formatters = formatters;
        this.columnNames = columnNames;
        outputRow = new String[columnNames.length];
    }

    public String[] getColumnNames() {
        return columnNames;
    }

    public void writeHeader(CSVWriter fileWriter) {
        fileWriter.writeRow(columnNames);
    }

    public void write(CSVWriter fileWriter) {
        fileWriter.writeRow(outputRow);
    }

    public void formatInt(int index, int value) throws UnsupportedFormatterException, ArrayIndexOutOfBoundsException {
        assertTypeAndIndex(index, com.hitorro.util.io.csv.formatters.FormatterType.Int);
        outputRow[index] = ((com.hitorro.util.io.csv.formatters.CSVIntFormatter) formatters[index]).format(value);
    }

    public void formatByte(int index, byte value) throws UnsupportedFormatterException, ArrayIndexOutOfBoundsException {
        assertTypeAndIndex(index, com.hitorro.util.io.csv.formatters.FormatterType.Byte);
        outputRow[index] = ((com.hitorro.util.io.csv.formatters.CSVByteFormatter) formatters[index]).format(value);
    }

    public void formatLong(int index, long value) throws UnsupportedFormatterException, ArrayIndexOutOfBoundsException {
        assertTypeAndIndex(index, com.hitorro.util.io.csv.formatters.FormatterType.Long);
        outputRow[index] = ((com.hitorro.util.io.csv.formatters.CSVLongFormatter) formatters[index]).format(value);
    }

    public void formatString(int index, String value) throws UnsupportedFormatterException, ArrayIndexOutOfBoundsException {
        assertTypeAndIndex(index, com.hitorro.util.io.csv.formatters.FormatterType.String);
        outputRow[index] = ((com.hitorro.util.io.csv.formatters.CSVStringFormatter) formatters[index]).format(value);
    }

    public void formatDate(int index, Date value) throws UnsupportedFormatterException, ArrayIndexOutOfBoundsException {
        assertTypeAndIndex(index, com.hitorro.util.io.csv.formatters.FormatterType.Date);
        outputRow[index] = ((com.hitorro.util.io.csv.formatters.CSVDateFormatter) formatters[index]).format(value);
    }

    public void formatBoolean(int index, boolean value) throws UnsupportedFormatterException, ArrayIndexOutOfBoundsException {
        assertTypeAndIndex(index, com.hitorro.util.io.csv.formatters.FormatterType.Boolean);
        outputRow[index] = ((com.hitorro.util.io.csv.formatters.CSVBooleanFormatter) formatters[index]).format(value);
    }

    private void assertTypeAndIndex(int index, com.hitorro.util.io.csv.formatters.FormatterType type) throws UnsupportedFormatterException, ArrayIndexOutOfBoundsException {
        if (index < 0 || index > formatters.length) {
            throw new ArrayIndexOutOfBoundsException();
        }
        if (formatters[index].getType() != type) {
            throw new UnsupportedFormatterException();
        }
    }
}
