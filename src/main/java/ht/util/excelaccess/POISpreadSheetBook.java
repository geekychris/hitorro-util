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

