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
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.hitorro.util.core.iterator.AbstractIterator;
import com.hitorro.util.core.string.StringUtil;

/**
 * Convert a csv row into a apply.
 */
public class CSV2MapIterator extends AbstractIterator<JsonNode> {
    private CSVIterator csvIter;
    private String header[];

    public CSV2MapIterator(CSVIterator iter) {
        csvIter = iter;
        header = csvIter.getColumnNames();
    }

    @Override
    public void close() throws Exception {
        csvIter.close();
    }

    @Override
    public boolean hasNext() {
        return csvIter.hasNext();
    }

    @Override
    public JsonNode next() {
        return mapIt();
    }

    @Override
    public void remove() {

    }

    private JsonNode mapIt() {
        if (csvIter.hasNext()) {
            String vals[] = csvIter.next();
            ObjectNode map = JsonNodeFactory.instance.objectNode();
            for (int i = 0; i < Math.min(header.length, vals.length); i++) {
                if (StringUtil.nullOrEmptyString(header[i])) {
                    continue;
                }
                if (vals[i] != null) {
                    map.put(header[i], vals[i]);
                }
            }

            return map;
        }
        return null;
    }
}
