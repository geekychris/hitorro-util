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

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.hitorro.util.core.iterator.json.HTJSONParser;
import com.hitorro.util.core.iterator.mappers.BaseMapper;
import com.hitorro.util.io.ResetableStringReader;

import java.io.IOException;

public class String2JsonMapper extends BaseMapper<String, JsonNode> {
    private final static JsonFactory factory = new JsonFactory();
    protected JsonParser parser = null;
    private ResetableStringReader reader = new ResetableStringReader("");

    public String2JsonMapper() {

    }

    public JsonNode apply(String s) {
        reader.set(s);
        try {
            HTJSONParser jnp = new HTJSONParser(factory.createParser(reader));
            return jnp.read();
        } catch (IOException e) {
            return null;
        }
    }

}
