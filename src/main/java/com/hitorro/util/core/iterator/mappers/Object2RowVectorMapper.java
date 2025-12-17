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
package com.hitorro.util.core.iterator.mappers;

import com.hitorro.util.core.Constants;

/**
 * Basic object to a vector of strings mapper.  Subclass me to handle the appropriate accessor function.
 */
public abstract class Object2RowVectorMapper<E> extends BaseMapper<E, String[]> {
    protected String[] keys;
    protected String[] target;
    protected int size;

    public Object2RowVectorMapper(String[][] tuples) {
        size = tuples.length;
        keys = new String[size];
        target = new String[size];
        for (int i = 0; i < size; i++) {
            keys[i] = tuples[i][0];
            target[i] = tuples[i][1];
        }
    }

    public Object2RowVectorMapper(String[] keys, String[] targets) {
        size = keys.length;
        this.keys = keys;
        this.target = targets;
    }

    public String[] getColumnNames() {
        return target;
    }

    @Override
    public String[] apply(final E e) {
        String result[] = new String[size];
        for (int i = 0; i < size; i++) {
            Object o = get(keys[i], e);
            if (o != null) {
                result[i] = o.toString();
            } else {
                result[i] = Constants.EmptyString;
            }
        }
        return result;
    }

    public abstract Object get(String field, E e);
}

