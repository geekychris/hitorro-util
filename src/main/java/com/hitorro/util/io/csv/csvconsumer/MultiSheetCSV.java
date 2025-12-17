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
package com.hitorro.util.io.csv.csvconsumer;

import com.hitorro.util.core.string.Fmt;
import com.hitorro.util.excelaccess.POICSVReader;
import com.hitorro.util.io.FileUtil;
import com.hitorro.util.io.csv.CSVReader;
import com.hitorro.util.io.filefilters.FilenameExtensionFilter;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Copyright (c) 2003 - present HiTorro All rights reserved. User: chris Date: Feb 9, 2005 Time: 10:25:19 AM
 * <p/>
 * Multi sheet csv reader assumes that you have a directory containing the sheets by name.
 */
public class MultiSheetCSV implements MultiSheetCSVReaderInterface {
    private File m_dir;
    private String m_fileExtension;
    private Map<String, File> m_map = new HashMap<String, File>();


    public MultiSheetCSV(File directory, String fileExtension) {
        m_dir = directory;
        m_fileExtension = fileExtension;
        init();
    }

    /**
     * Utility method that provides the correct multi sheet reader depending on if this is a directory or a file.  A
     * file is assumed to be an excel spreadsheet and a directory is assumed to be a collection of csv files.
     *
     * @param f
     * @return
     */
    public static MultiSheetCSVReaderInterface getReader(File f) {
        if (!f.exists()) {
            return null;
        }
        if (f.isDirectory()) {
            return new MultiSheetCSV(f, "csv");
        }
        return new POICSVReader(f);
    }

    private void init() {
        File files[] = m_dir.listFiles(new FilenameExtensionFilter(m_fileExtension, true));
        for (File f : files) {
            String name = FileUtil.getFileNameSansExtension(f);
            m_map.put(name.toLowerCase(), f);
        }
    }

    public void readLines(CSVConsumer consumer, String sheetName) throws IOException {
        File f = m_map.get(sheetName.toLowerCase());
        if (FileUtil.nullOrNotExist(f)) {
            throw new IOException(Fmt.S("Sheet %s does not exist", sheetName));
        }
        CSVReader reader = new CSVReader(f);
        reader.readLines(consumer);
    }
}
