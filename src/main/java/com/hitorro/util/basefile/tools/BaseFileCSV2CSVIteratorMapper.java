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
package com.hitorro.util.basefile.tools;

import com.hitorro.util.basefile.filters.FileEndsWith;
import com.hitorro.util.basefile.fs.BaseFile;
import com.hitorro.util.core.Log;
import com.hitorro.util.core.iterator.mappers.BaseMapper;
import com.hitorro.util.excelaccess.POICSVIterator;
import com.hitorro.util.io.csv.CSVIterator;
import com.hitorro.util.io.csv.CSVIteratorImpl;

import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Naive mapping from basefile to a csviterator.  You get some control if you think its a multi sheet xls file and you
 * want to control which sheet to read.
 */
public class BaseFileCSV2CSVIteratorMapper extends BaseMapper<BaseFile, CSVIterator> {
    private static final FileEndsWith XLSExt = new FileEndsWith("xls", true, true);

    private String sheet;
    private String encoding;
    private char seperator;

    public BaseFileCSV2CSVIteratorMapper(String sheet, String encoding) {
        this(sheet, encoding, ',');
    }

    public BaseFileCSV2CSVIteratorMapper(String sheet, String encoding, char seperator) {
        this.sheet = sheet;
        this.encoding = encoding;
        this.seperator = seperator;
    }

    @Override
    public CSVIterator apply(final BaseFile e) {
        if (XLSExt.test(e)) {
            try {
                return new POICSVIterator(BaseFileUtil.bf2inputstream.apply(e), sheet, true);
            } catch (IOException e1) {
                Log.io.error("Unable to construct POICSVIterator due to %s %e", e, e);
                return null;
            }
        } else {
            try {
                return new CSVIteratorImpl(new InputStreamReader(BaseFileUtil.bf2inputstream.apply(e), encoding), seperator);
            } catch (IOException e1) {
                Log.io.error("Unable to construct csviteratorimpl due to %s %e", e, e);
                return null;
            }
        }
    }
}
