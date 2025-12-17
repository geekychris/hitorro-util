package ht.util.excelaccess;


import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Copyright (c) 2003 - present HiTorro All rights reserved. User: chris Date: Jan 31, 2005 Time: 10:46:06 AM
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

