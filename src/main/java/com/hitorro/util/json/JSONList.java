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
package com.hitorro.util.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.hitorro.util.core.iterator.CollectionIterator;
import com.hitorro.util.json.visitors.JSONVisitor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class JSONList extends JSONElement<List<JSONElement>> {
    private List<JSONElement> list;

    public JSONList() {
        this.list = new ArrayList();
    }

    public JSONList(List<JSONElement> list) {
        this.list = list;
    }

    public CollectionIterator<JSONElement> iterate() {
        return new CollectionIterator(list);
    }

    public int size() {
        return list.size();
    }

    @Override
    public List<JSONElement> get() {
        return list;
    }

    @Override
    public JSONType getJSONType() {
        return JSONType.List;
    }

    @Override
    public int getAggregateSize() {
        int counter = 0;
        for (JSONElement elem : list) {
            counter += elem.getAggregateSize();
        }
        return counter;
    }

    @Override
    public void write(final JsonGenerator generator) throws IOException {
        generator.writeStartArray();
        for (JSONElement elem : list) {
            if (elem.getJSONType() == JSONType.Map) {
                generator.writeStartObject();
                elem.write(generator);
                generator.writeEndObject();
            } else {
                elem.write(generator);
            }
        }
        generator.writeEndArray();
    }

    @Override
    public void visit(final JSONVisitor visitor, int depth) {
        int child = depth + 1;
        for (JSONElement elem : list) {
            elem.visit(visitor, child);
        }
    }

    public void add(JSONElement elem) {
        list.add(elem);
    }

    public void add(JSONElement... elems) {
        for (JSONElement elem : elems) {
            list.add(elem);
        }
    }
}
