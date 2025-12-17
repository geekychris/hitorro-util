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


import com.hitorro.util.core.ListUtil;
import com.hitorro.util.core.string.StringUtil;
import com.hitorro.util.io.csv.csvconsumer.CSVConsumer;
import com.hitorro.util.io.csv.csvconsumer.MultiSheetCSVReaderInterface;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Copyright (c) 2003 - present HiTorro All rights reserved. User: chris Date: Jan 31, 2005 Time: 10:46:06 AM
 */
public class POICSVReader implements MultiSheetCSVReaderInterface {
    private CSVConsumer m_consumer;
    private POISpreadSheetBook m_book;
    private File m_file;

    public POICSVReader(File file) {
        m_file = file;
    }

    public POICSVReader(CSVConsumer consumerIn) {
        m_consumer = consumerIn;
    }

    public void read(String fileName)
            throws IOException {
        read(fileName, null);
    }

    public void readLines(CSVConsumer consumer, String sheetName)
            throws IOException {
        m_consumer = consumer;
        read(m_file, sheetName);
    }

    public void read(String fileName, String sheetName)
            throws IOException {
        read(new File(fileName), sheetName);
    }

    public void read(File fileName)
            throws IOException {
        read(fileName, null);
    }

    public boolean read(File fileName, String sheetName)
            throws IOException {
        POISpreadSheet spreadSheet = new POISpreadSheet();
        m_book = spreadSheet.getBook(fileName);
        m_file = fileName;
        return readBook(sheetName);
    }

    public void read(InputStream stream)
            throws IOException {
        read(stream, null);
    }

    public boolean read(InputStream stream, String sheetName)
            throws IOException {
        POISpreadSheet spreadSheet = new POISpreadSheet();
        m_book = spreadSheet.getBook(stream);
        m_file = null;
        return readBook(sheetName);
    }


    /**
     * @param sheetName
     * @return true if page exists and was read
     */
    private boolean readBook(String sheetName) {
        int numSheets = m_book.getSheetCount();
        if (StringUtil.nullOrEmptyOrBlankString(sheetName)) {
            for (int i = 0; i < numSheets; i++) {
                pageToRows(m_book.getSpreadSheet(i));
            }
            return true;
        } else {
            for (int i = 0; i < numSheets; i++) {
                POISpreadSheetSheet sheet = m_book.getSpreadSheet(i);
                String name = sheet.getName();
                if (name.equalsIgnoreCase(sheetName)) {
                    pageToRows(sheet);
                    return true;
                }
            }
        }
        return false;
    }

    private void pageToRows(POISpreadSheetSheet sheet) {
        int rows = sheet.getLastRow();
        for (int i = 0; i <= rows; i++) {
            List<String> v = rowToLine(sheet, i);

            if (!ListUtil.nullOrEmpty(v)) {
                writeVector(i, v);
            }
        }
    }

    private List<String> rowToLine(POISpreadSheetSheet sheet, int row) {
        int columns = sheet.getLastColumnForRow(row);
        List<String> v = new ArrayList();
        for (int column = 0; column <= columns; column++) {
            v.add(sheet.getText(row, column));
        }
        return v;
    }

    private void writeVector(int line, List<String> v) {
        String[] array = new String[v.size()];
        v.toArray(array);
        m_consumer.line(line, array);
    }


}
