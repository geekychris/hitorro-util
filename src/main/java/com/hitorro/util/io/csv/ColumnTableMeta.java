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

import gnu.trove.map.hash.TObjectIntHashMap;
import com.hitorro.util.core.Constants;

/**
 * <p/>
 * * Represents a wrapper of column name to index position into the a table. This is used so that column names can be
 * translated to index positions and from index positions to the content of a row of the table.
 */

public class ColumnTableMeta {
    private TObjectIntHashMap<String> m_columns = new TObjectIntHashMap<String>();
    private String[] columnNames;

    private ColumnTableMeta() {
        // Dont want anyone to init this way
    }

    public static ColumnTableMeta init(String[] line) {
        ColumnTableMeta result = new ColumnTableMeta();
        result.setColumnNames(line);
        if (line != null) {
            int length = line.length;
            for (int i = 0; i < length; i++) {
                String name = line[i];
                result.addColumn(name, Constants.getInteger(i));
            }
        }

        return result;
    }

    public int getSize() {
        return columnNames.length;
    }

    public String[] getColumnNames() {
        return columnNames;
    }

    private void setColumnNames(String line[]) {
        columnNames = line;
    }

    private void addColumn(String columnName, Integer index) {
        m_columns.put(columnName.trim(), index);
    }

    public int getColumnInteger(String columnName) {
        return m_columns.get(columnName);
    }

    public String get(String name, String row[]) {
        return row[getColumnInteger(name)];
    }

    public int getColumnInt(String columnName) {
        Integer i = getColumnInteger(columnName.trim());
        if (i != null) {
            return i.intValue();
        }
        return -1;
    }
}
