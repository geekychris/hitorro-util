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


import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 */
public class POICSVWriter {
    private POISpreadSheet spreadSheet;
    private POISpreadSheetSheet sheet;
    private POISpreadSheetBook book;
    private File file;
    private int rowNumber = 0;
    private int sheetNumber;
    private int excelFormat;
    private int maxRowsPerSheet;

    public POICSVWriter(File fileIn, int excelFormat) {
        spreadSheet = new POISpreadSheet();
        book = spreadSheet.newBook();
        sheet = book.newSheet();
        sheetNumber = 0;
        file = fileIn;
        this.excelFormat = excelFormat;
        maxRowsPerSheet = ExcelUtil.getMaxRowsPerSheet(excelFormat);

    }

    public void writeLine(List vector) {
        for (int i = 0, s = vector.size(); i < s; i++) {
            String string = (String) vector.get(i);
            sheet.setText(rowNumber, i, string);
        }
        rowNumber++;

        if (rowNumber % maxRowsPerSheet == 0) {
            Log.excel.debug("SpreadSheetCSVWrite: spread sheet %s is full. " +
                    "writing to the next sheet.", sheetNumber);
            sheetNumber++;
            sheet = book.newSheet();
            rowNumber = 0;
        }
    }

    /**
     * closes the stream.
     */
    public void close()
            throws IOException {
        try {
            book.saveSpreadSheetBook(file.getAbsolutePath());
        } finally {

        }
    }
}

