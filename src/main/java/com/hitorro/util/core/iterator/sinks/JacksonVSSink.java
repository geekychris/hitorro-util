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
package com.hitorro.util.core.iterator.sinks;

import com.fasterxml.jackson.databind.JsonNode;
import com.hitorro.util.basefile.fs.BaseFile;
import com.hitorro.util.core.iterator.JsonValueSource;

import java.io.IOException;
import java.io.OutputStream;

/**
 *
 */
public class JacksonVSSink extends BaseSink<JsonValueSource> {
    private JsonSink sink;

    public JacksonVSSink(BaseFile bf) throws IOException {
        sink = new JsonSink(bf);
    }

    public JacksonVSSink(OutputStream os) {
        sink = new JsonSink(os);
    }

    @Override
    public boolean init(JsonNode node) {
        return sink.init(node);
    }

    @Override
    public boolean start() {
        return sink.start();
    }

    public boolean add(JsonValueSource vs) {
        return sink.add(vs.getNode());
    }

    @Override
    public boolean stop() throws IOException {
        return sink.stop();
    }
}
