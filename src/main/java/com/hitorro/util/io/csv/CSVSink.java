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

import com.fasterxml.jackson.databind.JsonNode;
import com.hitorro.util.basefile.fs.BaseFile;
import com.hitorro.util.core.Log;
import com.hitorro.util.core.iterator.sinks.BaseSink;
import com.hitorro.util.core.string.StringUtil;
import com.hitorro.util.io.StoreException;
import com.hitorro.util.json.keys.BasefileProperty;
import com.hitorro.util.json.keys.StringListFromDelimitedKey;
import com.hitorro.util.json.keys.StringProperty;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class CSVSink extends BaseSink<String[]> {
    public static final BasefileProperty OutputFileKey = new BasefileProperty("file", "outputfile using basefile syntax");
    public static final StringListFromDelimitedKey ColumnsKey = new StringListFromDelimitedKey("columns", "", ",", null);
    public static final StringProperty SepKey = new StringProperty("seperator", "", ",");
    private CSVWriter writer;

    public CSVSink() {
        // for init config driven instantiation only.
    }

    public CSVSink(String columns[], OutputStream os, char seperator) throws UnsupportedEncodingException {
        init(columns, os, "UTF-8", seperator);
    }

    public CSVSink(String columns[], OutputStream os, String encoding, char seperator) throws UnsupportedEncodingException {
        init(columns, os, encoding, seperator);
    }

    private void init(final String[] columns, final OutputStream os, final String encoding, char seperator) throws UnsupportedEncodingException {
        List<String> cols = new ArrayList();
        for (String s : columns) {
            cols.add(s);
        }
        PrintStream pw = new PrintStream(os, true, encoding);
        writer = new CSVFileWriter(pw, cols, seperator);
    }

    @Override
    public boolean init(JsonNode node) {
        BaseFile bf = OutputFileKey.apply(node);
        List<String> cols = ColumnsKey.apply(node);
        String sep = SepKey.apply(node);
        char seperator = ',';
        if (!StringUtil.nullOrEmptyString(sep)) {
            seperator = sep.charAt(0);
        }
        try {
            init(cols.toArray(new String[cols.size()]), bf.getDataOutputStream(), "UTF-8", seperator);
        } catch (IOException e) {
            Log.util.error("Unable to initialize CSVSink", e, e);
            return false;
        }
        return true;
    }

    @Override
    public boolean start() {
        return true;
    }

    @Override
    public boolean add(final String[] o) throws IOException, StoreException {
        writer.writeRow(o);
        return true;
    }

    @Override
    public boolean stop() throws IOException {
        writer.close();
        return true;
    }
}
