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
package ht.util.io.csv;

import ht.util.io.csv.formatters.*;

import java.util.Date;

/**
 * Copyright (c) 2003 - present HiTorro All rights reserved. User: chris Date: Jan 16, 2008 Time: 7:21:39 AM
 */
public class CSVFormattedWriter {
    private CSVFormatter[] formatters;
    private String[] columnNames;
    private Object[] outputRow;

    public CSVFormattedWriter(String columnNames[], CSVFormatter formatters[]) {
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
        assertTypeAndIndex(index, FormatterType.Int);
        outputRow[index] = ((CSVIntFormatter) formatters[index]).format(value);
    }

    public void formatByte(int index, byte value) throws UnsupportedFormatterException, ArrayIndexOutOfBoundsException {
        assertTypeAndIndex(index, FormatterType.Byte);
        outputRow[index] = ((CSVByteFormatter) formatters[index]).format(value);
    }

    public void formatLong(int index, long value) throws UnsupportedFormatterException, ArrayIndexOutOfBoundsException {
        assertTypeAndIndex(index, FormatterType.Long);
        outputRow[index] = ((CSVLongFormatter) formatters[index]).format(value);
    }

    public void formatString(int index, String value) throws UnsupportedFormatterException, ArrayIndexOutOfBoundsException {
        assertTypeAndIndex(index, FormatterType.String);
        outputRow[index] = ((CSVStringFormatter) formatters[index]).format(value);
    }

    public void formatDate(int index, Date value) throws UnsupportedFormatterException, ArrayIndexOutOfBoundsException {
        assertTypeAndIndex(index, FormatterType.Date);
        outputRow[index] = ((CSVDateFormatter) formatters[index]).format(value);
    }

    public void formatBoolean(int index, boolean value) throws UnsupportedFormatterException, ArrayIndexOutOfBoundsException {
        assertTypeAndIndex(index, FormatterType.Boolean);
        outputRow[index] = ((CSVBooleanFormatter) formatters[index]).format(value);
    }

    private void assertTypeAndIndex(int index, FormatterType type) throws UnsupportedFormatterException, ArrayIndexOutOfBoundsException {
        if (index < 0 || index > formatters.length) {
            throw new ArrayIndexOutOfBoundsException();
        }
        if (formatters[index].getType() != type) {
            throw new UnsupportedFormatterException();
        }
    }
}
