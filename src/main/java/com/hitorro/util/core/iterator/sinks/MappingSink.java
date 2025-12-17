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
import java.util.function.Function;

/**
 * Yet another place to shuv a mapper (in the put??? what???). Well if you couple sinks together using such things as
 * SplitSink you may want to seperate your iterator of objects up before you convert to some other thing OR you may
 * simply want to use a apply as something that has a side such as count things.
 */
public class MappingSink<I, O> extends BaseSink<I> {
    private Sink<O> sink;

    private Function<I, O> mapper;

    public MappingSink(Sink<O> sink, Function<I, O> mapper) {
        this.sink = sink;
        this.mapper = mapper;
    }

    @Override
    public boolean init(JsonNode node) {
        return sink.init(node);
    }

    @Override
    public boolean start() throws IOException {
        return sink.start();
    }

    @Override
    public boolean add(final I o) throws IOException, StoreException {
        return sink.add(mapper.apply(o));
    }

    @Override
    public boolean stop() throws IOException {
        return sink.stop();
    }
}
