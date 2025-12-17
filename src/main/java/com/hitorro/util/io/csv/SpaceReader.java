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
import com.hitorro.util.basefile.tools.BaseFileUtil;
import com.hitorro.util.core.iterator.CloseableIterator;
import com.hitorro.util.core.string.StringUtil;
import com.hitorro.util.io.csv.csvconsumer.CSVConsumer;

/**
 * Dumb reader that takes lines like:
 * <p/>
 * chris       1.222  2.22 3
 * <p/>
 * and turns them into an array of {"chris", "1.222", "2.22", "3}
 */
public class SpaceReader {
    private CloseableIterator<String> iter;

    public SpaceReader(BaseFile inputFile) {
        iter = BaseFileUtil.bf2lineiter.apply(inputFile);
    }

    public void consume(CSVConsumer consumer) {
        int row = 0;
        while (iter.hasNext()) {
            String s = iter.next();
            String parts[] = StringUtil.tokenizeFromSingleChar(s, " ", true);
            consumer.line(row++, parts);
        }
    }
}
