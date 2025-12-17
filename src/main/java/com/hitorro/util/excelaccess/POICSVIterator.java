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

import com.hitorro.util.basefile.fs.BaseFile;
import com.hitorro.util.core.iterator.AbstractIterator;
import com.hitorro.util.io.FileUtil;
import com.hitorro.util.io.csv.CSVIterator;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * Iterate through a sheet of an excel spreadsheet in a naive way.  We assume the first row are the headings
 */
public class POICSVIterator extends AbstractIterator<String[]> implements CSVIterator {
    private POISpreadSheetBook m_book;
    private POISpreadSheetSheet sheet = null;
    private String header[];
    private int rows;
    private int i;

    public POICSVIterator(File file, String sheetName, boolean hasHeader) throws IOException {
        setupBook(FileUtil.getBufferedFileInputStream(file), sheetName, hasHeader);

    }

    public POICSVIterator(BaseFile file, String sheetName, boolean hasHeader) throws IOException {
        setupBook(file.getDataInputStream(), sheetName, hasHeader);
    }

    public POICSVIterator(InputStream is, String sheetName, boolean hasHeader) throws IOException {
        setupBook(is, sheetName, hasHeader);
    }

    public POICSVIterator() {
    }

    public String[] getColumnNames() {
        return header;
    }

    public boolean setupBook(InputStream stream, String sheetName, boolean hasHeader)
            throws IOException {
        POISpreadSheet spreadSheet = new POISpreadSheet();
        m_book = spreadSheet.getBook(stream);
        if (!setupBook(sheetName)) {
            return false;
        }
        if (hasNext()) {
            header = next();
        }
        return false;
    }


    /**
     * @param sheetName
     * @return true if page exists and was read
     */
    private boolean setupBook(String sheetName) {
        int numSheets = m_book.getSheetCount();

        for (int i = 0; i < numSheets; i++) {
            POISpreadSheetSheet s = m_book.getSpreadSheet(i);
            String name = s.getName();
            if (sheetName == null || name.equalsIgnoreCase(sheetName)) {
                sheet = s;
                rows = sheet.getLastRow();
                return true;
            }
        }
        return false;
    }

    private String[] rowToLine() {
        int columns = sheet.getLastColumnForRow(i);
        String s[] = new String[columns + 1];
        for (int column = 0; column <= columns; column++) {
            s[column] = (sheet.getText(i, column));
        }
        i++;
        return s;
    }

    @Override
    public void close() throws Exception {
    }

    @Override
    public boolean hasNext() {
        return i <= rows;
    }

    @Override
    public String[] next() {
        return rowToLine();
    }

    @Override
    public void remove() {
    }
}
