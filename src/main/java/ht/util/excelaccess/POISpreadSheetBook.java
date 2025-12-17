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
package ht.util.excelaccess;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;


/**
 * Copyright (c) 2003 - present HiTorro All rights reserved. User: chris Date: Jan 31, 2005 Time: 10:46:06 AM
 */
public class POISpreadSheetBook {
    private HSSFWorkbook workbook;

    public POISpreadSheetBook(HSSFWorkbook bookIn) {
        workbook = bookIn;
    }

    public int getSheetCount() {
        return workbook.getNumberOfSheets();
    }

    public POISpreadSheetSheet getSpreadSheet(int sheetNumber)
            throws DetailedSpreadSheetException {
        try {
            HSSFSheet sheet = workbook.getSheetAt(sheetNumber);
            return new POISpreadSheetSheet(sheet, workbook, sheetNumber);
        } catch (IndexOutOfBoundsException e) {
            throw new DetailedSpreadSheetException(e, sheetNumber, -1, -1);
        }
    }

    public void saveSpreadSheetBook(String fileName)
            throws IOException {
        File outputFile = new File(fileName);
        FileOutputStream out = new FileOutputStream(outputFile);
        workbook.write(out);
        out.close();
    }

    public POISpreadSheetSheet newSheet() {
        int count = getSheetCount();
        HSSFSheet newSheet = workbook.createSheet();
        // use the previous count as the new sheet's sheetNumber
        return new POISpreadSheetSheet(newSheet, workbook, count);
    }
}

