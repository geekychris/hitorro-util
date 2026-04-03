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
import com.hitorro.util.io.StoreException;

import java.io.IOException;
import java.util.function.Predicate;

public class PredicatedSink<T> implements Sink<T> {
    private Sink<T> sink;
    private Predicate<T> predicate;

    public PredicatedSink(Sink<T> sink, Predicate<T> predicate) {
        this.sink = sink;
        this.predicate = predicate;
    }

    @Override
    public boolean init(final JsonNode node) {
        return true;
    }

    @Override
    public boolean start() throws IOException {
        return sink.start();
    }

    @Override
    public boolean add(final T o) throws IOException, StoreException {
        if (predicate.test(o)) {
            return this.sink.add(o);
        }
        return false;
    }

    @Override
    public boolean stop() throws IOException {
        return sink.stop();
    }

    @Override
    public void close() throws IOException {
        this.sink.close();
    }
}
