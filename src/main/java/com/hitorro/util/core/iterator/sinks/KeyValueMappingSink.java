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
import com.hitorro.util.core.GenericKeyValue;
import com.hitorro.util.io.StoreException;

import java.io.IOException;

/**
 * Special Sink that is given two inputStream to K and V data sink mappers.
 * It will then write the K and the V to that file.  The Sinks MUST
 * ensure that they are writing all their record out to the stream
 */
public class KeyValueMappingSink<K, V> extends BaseSink<GenericKeyValue<K, V>> {
    private Sink<K> kSink;
    private Sink<V> vSink;

    public KeyValueMappingSink() {

    }

    public KeyValueMappingSink(Sink<K> keySink, Sink<V> valueSink) {
        setSinks(keySink, valueSink);
    }

    public void setSinks(Sink<K> keySink, Sink<V> valueSink) {
        kSink = keySink;
        vSink = valueSink;
    }

    @Override
    public boolean init(JsonNode node) {
        return false;
    }

    @Override
    public boolean start() throws IOException {
        kSink.start();
        vSink.start();
        return true;
    }

    @Override
    public boolean add(final GenericKeyValue<K, V> o) throws IOException, StoreException {
        kSink.add(o.getKey());
        vSink.add(o.getValue());
        return true;
    }

    @Override
    public boolean stop() throws IOException {
        kSink.stop();
        vSink.stop();
        return true;
    }
}
