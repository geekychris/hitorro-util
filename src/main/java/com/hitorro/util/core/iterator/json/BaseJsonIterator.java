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
package com.hitorro.util.core.iterator.json;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.hitorro.util.basefile.fs.BaseFile;
import com.hitorro.util.core.iterator.AbstractIterator;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;


/**
 *
 */
public class BaseJsonIterator extends AbstractIterator<JsonNode> {
    private final static JsonFactory factory = new JsonFactory();

    protected HTJSONParser jnparser;
    protected JsonParser parser = null;
    protected JsonNode domlet;

    protected boolean closed = false;

    public BaseJsonIterator(JsonParser parser, boolean switchOnToken) {
        init(parser);
    }

    public BaseJsonIterator(BaseFile baseFile) throws IOException {
        init(factory.createParser(baseFile.getInputStreamRaw()));
    }

    public BaseJsonIterator(InputStream is) throws IOException {
        init(factory.createParser(is));
    }

    public BaseJsonIterator(Reader reader) throws IOException {
        init(factory.createParser(reader));
    }

    private void init(final JsonParser parser) {
        this.parser = parser;
        jnparser = new HTJSONParser(parser);
    }

    @Override
    public void close() throws Exception {
        if (parser != null) {
            parser.close();
        }
        super.close();
    }

    public boolean hasNext() {
        if (closed) {
            return false;
        }
        if (domlet == null) {
            domlet = jnparser.read();
            if (domlet == null) {
                closed = true;
                return false;
            }
        }
        return true;
    }

    public JsonNode next() {
        JsonNode dom = domlet;
        domlet = null;
        return dom;
    }

    public void remove() {

    }


}
