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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hitorro.util.core.Constants;
import com.hitorro.util.core.iterator.mappers.BaseMapper;
import com.hitorro.util.core.thread.ThreadStash;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

public class Json2StringMapper extends BaseMapper<JsonNode, String> {
    public static ThreadStash<Json2StringMapper> threadedMapper = new ThreadStash() {
        public Json2StringMapper getNew() {
            return new Json2StringMapper();
        }
    };

    protected ObjectMapper mapper = new ObjectMapper();

    @Override
    public String apply(final JsonNode jsonNode) {
        ByteArrayOutputStream boas = new ByteArrayOutputStream();
        try {
            OutputStreamWriter ow = new OutputStreamWriter(boas, Constants.UTF8);
            mapper.writeValue(ow, jsonNode);
            boas.flush();
        } catch (IOException e) {
            return null;
        }
        return new String(boas.toByteArray());
    }
}