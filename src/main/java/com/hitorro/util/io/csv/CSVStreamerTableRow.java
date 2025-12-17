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


/**
 * Copyright (c) 2003-2008 HiTorro.
 * <p/>
 * User: chris Date: Apr 18, 2004 Time: 1:07:20 PM
 * <p/>
 * Description:
 */
public class CSVStreamerTableRow implements StreamerTable {
    private ColumnTableMeta m_meta;
    private String[] m_row;

    public ColumnTableMeta getMeta() {
        return m_meta;
    }

    public void setMeta(ColumnTableMeta meta) {
        m_meta = meta;
    }

    public String[] getRow() {
        return m_row;
    }

    public void setRow(String[] row) {
        m_row = row;
    }
}
