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
import java.util.LinkedList;
import java.util.Queue;

/**
 *
 */
public class TeeSink<I> extends BaseSink<I> {
    protected Sink<I> sink1;
    protected Sink<I> sink2;

    public TeeSink(Sink<I> sink1, Sink<I> sink2) {
        this.sink1 = sink1;
        this.sink2 = sink2;
    }

    public static <I> Sink<I> get(Sink<I>... sinks) {
        Queue<Sink<I>> queue = new LinkedList<Sink<I>>();
        for (Sink<I> s : sinks) {
            ((LinkedList<Sink<I>>) queue).add(s);
        }
        while (queue.size() > 1) {
            queue.add(new TeeSink(queue.remove(), queue.remove()));
        }
        return ((LinkedList<Sink<I>>) queue).get(0);
    }

    @Override
    public boolean init(JsonNode node) {
        boolean success = sink1.init(node);
        success |= sink2.init(node);
        return success;
    }

    @Override
    public boolean start() throws IOException {
        boolean success = sink1.start();
        success |= sink2.start();
        return success;
    }

    @Override
    public boolean add(final I o) throws IOException, StoreException {
        sink1.add(o);
        sink2.add(o);
        return true;
    }

    @Override
    public boolean stop() throws IOException {
        boolean success = sink1.stop();
        success |= sink2.stop();
        return success;
    }
}
