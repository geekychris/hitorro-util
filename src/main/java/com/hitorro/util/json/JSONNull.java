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
import com.hitorro.util.json.visitors.JSONVisitor;

import java.io.IOException;

/**
 *
 */
public class JSONNull extends JSONElement<Object> {
    public static final JSONNull me = new JSONNull();

    @Override
    public Object get() {
        return this;
    }

    @Override
    public JSONType getJSONType() {
        return JSONType.Null;
    }

    @Override
    public void visit(final JSONVisitor visitor, final int depth) {
    }

    public int getAggregateSize() {
        return 8;
    }

    @Override
    public void write(final JsonGenerator generator) throws IOException {
        generator.writeNull();
    }
}
