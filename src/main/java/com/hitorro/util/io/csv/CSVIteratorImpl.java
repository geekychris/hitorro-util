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
import com.hitorro.util.core.Log;
import com.hitorro.util.core.iterator.AbstractIterator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;


/**
 *
 */
public class CSVIteratorImpl extends AbstractIterator<String[]> implements CSVIterator {
    private String[] line;
    private ColumnTableMeta meta = null;

    private CSVReaderBase rb;

    public CSVIteratorImpl(BaseFile filepath, String encoding) throws IOException {
        rb = new CSVReaderBase(filepath, encoding);
        readNextLineAux();
    }

    public CSVIteratorImpl(BaseFile filepath, char encoding) throws IOException {
        rb = new CSVReaderBase(filepath, "UTF-8", encoding);
        readNextLineAux();
    }

    public CSVIteratorImpl(File filepath, char seperator) throws FileNotFoundException {
        rb = new CSVReaderBase(filepath, seperator);
        readNextLineAux();
    }

    public CSVIteratorImpl(Reader rdr, char seperator) {
        rb = new CSVReaderBase(rdr, seperator);
        readNextLineAux();
    }

    public ColumnTableMeta getMeta() {
        if (meta == null) {
            meta = ColumnTableMeta.init(getColumnNames());
        }
        return meta;
    }

    public void enableColumnFixup(boolean flag) {
        rb.adjustColumns = flag;
    }

    private void readNextLineAux() {
        try {
            line = null;
            line = rb.getNextRow();
        } catch (IOException e) {
            Log.io.error("Unable to read from csv  %s %e", e, e);
        }
    }

    @Override
    public boolean hasNext() {
        return line != null;
    }

    @Override
    public String[] next() {
        String ret[] = line;
        readNextLineAux();
        return ret;
    }

    @Override
    public void remove() {
    }

    @Override
    public String[] getColumnNames() {
        return rb.getColumnNames();
    }

    @Override
    public void close() throws Exception {
        rb.close();
    }
}
