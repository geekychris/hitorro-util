package ht.util.excelaccess;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;


/**
 * Copyright (c) 2003 - present HiTorro All rights reserved. User: chris Date: Jan 31, 2005 Time: 10:46:06 AM
 */
public class POISpreadSheet {
    public POISpreadSheetBook getBook(File file)
            throws IOException {
        return getBook(new FileInputStream(file));
    }


    public POISpreadSheetBook getBook(InputStream inputStream)
            throws IOException {
        POIFSFileSystem fileSystem = new POIFSFileSystem(inputStream);
        HSSFWorkbook book = new HSSFWorkbook(fileSystem);
        return new POISpreadSheetBook(book);
    }

    public POISpreadSheetBook getBook(String fileName)
            throws IOException {
        return getBook(new File(fileName));
    }

    public POISpreadSheetBook newBook() {
        HSSFWorkbook book = new HSSFWorkbook();
        return new POISpreadSheetBook(book);
    }


}

