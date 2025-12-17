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
package com.hitorro.util.core.math;

import com.hitorro.util.basefile.fs.BaseFile;
import com.hitorro.util.io.csv.ColumnTableMeta;
import com.hitorro.util.io.csv.query.CSVQuery;

/**
 * set of sparse counters addressed by an index position.  Simple convenience mechanism to
 */
public class SparseCounterSet {
    private SparseCounter[] sc;

    private BaseFile bf;
    private boolean sparse;
    private String rootName;

    public SparseCounterSet(BaseFile bf, String rootName, boolean sparse, int size) {
        this.bf = bf;
        this.sparse = sparse;
        this.rootName = rootName;
        sc = new SparseCounter[size];
    }

    public void addFromMeta(ColumnTableMeta meta, String fields[]) {
        // has to be the size of the columns as we are given
        sc = new SparseCounter[meta.getSize()];
        for (String col : fields) {
            add(col, meta.getColumnInt(col));
        }
    }

    public void addFromMeta(CSVQuery meta, String fields[]) {
        // has to be the size of the columns as we are given
        sc = new SparseCounter[fields.length];
        // keep index position of the fields
        for (int i = 0; i < fields.length; i++) {
            add(fields[i], i);
        }
    }

    public void add(String name, int pos) {
        sc[pos] = new SparseCounter(name, "ord", "freq");
    }

    public void increment(int pos, int amount) {
        if (pos >= sc.length) {
            return;
        }
        if (sc[pos] != null) {
            sc[pos].increment(amount);
        }
    }

    public void write() {
        for (int i = 0; i < sc.length; i++) {
            if (sc[i] != null) {
                sc[i].write(bf, rootName, sparse);
            }
        }
    }
}

