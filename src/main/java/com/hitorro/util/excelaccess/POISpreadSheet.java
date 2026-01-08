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

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;


/**
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

