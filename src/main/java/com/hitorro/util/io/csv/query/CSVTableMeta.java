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
package com.hitorro.util.io.csv.query;


import com.hitorro.util.core.UtilDataType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class CSVTableMeta {
    private List<CSVTableMetaItem> cols = new ArrayList();
    private Map<String, CSVTableMetaItem> map = new HashMap();

    public void add(String field, UtilDataType dt) {
        field = field.toLowerCase();
        CSVTableMetaItem item = new CSVTableMetaItem();
        item.setField(field);
        item.setDt(dt);
        cols.add(item);
        map.put(field, item);
    }

    public int getSize() {
        return cols.size();
    }

    public void addAll(String fields[], UtilDataType dts[]) {
        for (int i = 0; i < fields.length; i++) {
            add(fields[i], dts[i]);
        }
    }

    public CSVTableMetaItem get(String field) {
        return map.get(field.toLowerCase());
    }

    public CSVTableMetaItem get(int i) {
        return cols.get(i);
    }
}
