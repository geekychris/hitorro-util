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
import java.util.ArrayList;
import java.util.List;

/**
 * Dump elements to a listFiles for recovery later.
 */
public class SinkList<E> implements Sink<E> {
    private List<E> list = new ArrayList();

    @Override
    public boolean init(JsonNode node) {
        return true;
    }

    @Override
    public boolean start() {
        list.clear();
        return true;
    }

    @Override
    public boolean add(final E o) throws IOException, StoreException {
        list.add(o);
        return true;
    }

    @Override
    public boolean stop() throws IOException {
        return true;
    }

    @Override
    public void close() throws IOException {
        //
    }

    public List<E> getList() {
        return list;
    }
}
