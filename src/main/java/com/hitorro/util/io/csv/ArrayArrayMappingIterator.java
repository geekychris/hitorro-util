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

import com.hitorro.util.core.iterator.AbstractIterator;
import com.hitorro.util.core.iterator.Mapper;

import java.io.IOException;
import java.util.Iterator;

/**
 * Map a string array to an object array where the output may not be the same cardinality as the input
 */
public class ArrayArrayMappingIterator extends AbstractIterator<Object[]> {
    private Iterator<String[]> iter;
    private Mapper<String, Object> mappers[];
    private int indices[];

    public ArrayArrayMappingIterator(Iterator<String[]> iter, Mapper<String, Object> mappers[], int indices[]) {
        this.iter = iter;
        this.mappers = mappers;
        this.indices = indices;
    }

    @Override
    public void close() throws IOException {
    }

    @Override
    public boolean hasNext() {
        return iter.hasNext();
    }

    @Override
    public Object[] next() {
        String row[] = iter.next();
        Object out[] = new Object[indices.length];
        for (int i = 0; i < indices.length; i++) {
            out[i] = mappers[i].apply(row[indices[i]]);
        }
        return out;
    }

    @Override
    public void remove() {

    }
}
