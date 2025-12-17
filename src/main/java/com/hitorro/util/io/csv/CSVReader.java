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

package com.hitorro.util.io.csv;

import com.hitorro.util.basefile.fs.BaseFile;
import com.hitorro.util.io.csv.csvconsumer.CSVConsumer;

import java.io.*;
import java.nio.charset.Charset;

public class CSVReader extends CSVReaderBase {
    public CSVReader(String filepath) throws FileNotFoundException {
        super(filepath);
    }

    public CSVReader(File inFile) throws FileNotFoundException {
        super(inFile);
    }

    public CSVReader(BaseFile filepath, String encoding) throws IOException {
        super(filepath, encoding);
    }

    public CSVReader(BaseFile filepath, String encoding, char seperator) throws IOException {
        super(filepath, encoding, seperator);
    }

    public CSVReader(File inFile, char seperator) throws FileNotFoundException {
        super(inFile, seperator);
    }

    public CSVReader(InputStream inStream) {
        super(inStream);
    }

    public CSVReader(InputStream inStream, Charset cset) {
        super(inStream, cset);
    }

    public CSVReader(Reader rdr, char seperator) {
        super(rdr, seperator);
    }

    public CSVReader(Reader rdr) {
        super(rdr);
    }


    /**
     * Send all the data from the csv file to a CSV consumer. The CSVConsumer will first be handed a line containing the
     * column names and then will be handed all the remaning lines of the file.  Conventionally this method is called
     * immediately after constructing the CSVReader, in which case all the lines are sent to the consumer.  The
     * CSVReader should still be closed when the routine finishes
     *
     * @param consumer the consumer which will be given the file's data
     */
    public void readLines(CSVConsumer consumer)
            throws IOException {
        int row = 0;
        if (m_colNames != null) {
            consumer.line(row++, m_colNames);
        }
        // read the rest of the file, parsing the lines, and
        // handing the data to the consumer
        String[] line = getNextRow();
        while (line != null) {
            consumer.line(row++, line);
            line = getNextRow();
        }
    }
}
