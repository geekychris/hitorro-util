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
package com.hitorro.util.json.iterators;

import com.hitorro.util.core.iterator.AbstractIterator;
import com.hitorro.util.core.iterator.CollectionIterator;
import com.hitorro.util.core.iterator.mappers.BaseMapper;
import com.hitorro.util.json.JSONElement;
import com.hitorro.util.json.JSONList;

/**
 * Mapper used in "nesting" an iterator from another iterator of json element.
 * <p/>
 * Could be used outside the nesting iterator.  Great for getting a set of json elements from one blob.
 * <p/>
 * You provide the root path to the JSONList element you want to iterate over.
 */
public class GetJSONIteratorFromPath extends BaseMapper<JSONElement, AbstractIterator<JSONElement>> {
    private String path;

    public GetJSONIteratorFromPath(String path) {
        this.path = path;
    }

    @Override
    public AbstractIterator<JSONElement> apply(final JSONElement e) {
        JSONElement e2 = e.getFromPath(path);
        if (e2 instanceof JSONList) {
            // just give back a one element iterator (its probably an error)
            return new CollectionIterator(((JSONList) e2).get());

        }
        return null;
    }
}