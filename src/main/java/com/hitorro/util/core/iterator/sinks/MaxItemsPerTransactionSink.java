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
import java.util.function.Consumer;

public class MaxItemsPerTransactionSink<E> implements Sink<E> {
    private long counter;
    private long maxPerTransaction;
    private Sink<E> in;
    private boolean started = false;
    private Consumer<Sink<E>> postCloseConsumer = null;

    public MaxItemsPerTransactionSink(Sink<E> in, long maxItems) {
        this(in, maxItems, null);
    }

    public MaxItemsPerTransactionSink(Sink<E> in, long maxItems, Consumer<Sink<E>> postCloseConsumer) {
        this.in = in;
        this.maxPerTransaction = maxItems;
        this.postCloseConsumer = postCloseConsumer;
        resetCounter();

    }

    private void resetCounter() {
        counter = maxPerTransaction;
    }

    @Override
    public boolean init(final JsonNode node) {
        return true;
    }

    @Override
    public boolean start() throws IOException {
        resetCounter();
        started = true;
        return in.start();
    }

    @Override
    public boolean add(final E o) throws IOException, StoreException {
        if (!started) {
            start();
        }
        if (counter == 0) {
            in.stop();
            postStopProcessor();
            in.start();
            resetCounter();
        }
        counter--;
        return in.add(o);
    }

    @Override
    public boolean stop() throws IOException {
        started = false;
        return in.stop();
    }

    @Override
    public void close() throws IOException {
        in.close();
        postStopProcessor();
    }

    private void postStopProcessor() {
        if (postCloseConsumer != null) {
            postCloseConsumer.accept(in);
        }
    }
}
