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
package com.hitorro.util.excelaccess;

import com.hitorro.util.core.string.Fmt;


/**
 */
public class DetailedSpreadSheetException extends RuntimeException {
    private String m_description;
    private int sheetNum;
    private int m_row;
    private int m_column;

    public DetailedSpreadSheetException(Exception description, int sheetNum, int row, int column) {
        super(Fmt.S("%s sheet: %s row: %s column: %s", description.toString(), Integer.toString(sheetNum), Integer.toString(row), Integer.toString(column)));
        m_description = description.toString();
        this.sheetNum = sheetNum;
        m_row = row;
        m_column = column;
    }

    public DetailedSpreadSheetException(String description, int sheetNum, int row, int column) {
        super(Fmt.S("%s sheet: %s row: %s column: %s", description, Integer.toString(sheetNum), Integer.toString(row), Integer.toString(column)));
        m_description = description;
        this.sheetNum = sheetNum;
        m_row = row;
        m_column = column;
    }

}
