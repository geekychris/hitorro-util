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
package com.hitorro.util.json.mapper;

import com.hitorro.util.core.iterator.AbstractIterator;
import com.hitorro.util.core.iterator.ArrayIterator;
import com.hitorro.util.core.iterator.mappers.BaseMapper;
import com.hitorro.util.json.JSONElement;
import com.hitorro.util.json.JSONType;
import com.hitorro.util.json.iterators.JSONIterFromJSONList;

/**
 *
 */
public class JSONIterIterMapper extends BaseMapper<JSONElement, AbstractIterator<JSONElement>> {
    public static final JSONIterIterMapper jsonIterIterMapper = new JSONIterIterMapper();

    @Override
    public AbstractIterator<JSONElement> apply(final JSONElement e) {
        if (e.getJSONType() == JSONType.List) {
            return new JSONIterFromJSONList(e);
        } else {
            // just give back a one element iterator (its probably an error)
            return new ArrayIterator(new JSONElement[]{e});
        }
    }
}
