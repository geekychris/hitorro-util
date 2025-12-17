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
import com.hitorro.util.core.iterator.JsonValueSource;
import com.hitorro.util.core.iterator.mappers.BaseMapper;

/**
 * Wrapper a csviterator in a json iterator
 */
public class CSVIterator2JSONMapper extends BaseMapper<CSVIterator, AbstractIterator<JsonValueSource>> {
    public static final CSVIterator2JSONMapper me = new CSVIterator2JSONMapper();

    @Override
    public AbstractIterator<JsonValueSource> apply(final CSVIterator e) {
        return new CSV2JSONIterator(e);
    }
}
