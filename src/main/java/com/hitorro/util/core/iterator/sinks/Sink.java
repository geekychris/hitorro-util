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
import com.hitorro.util.json.JsonInitable;

import java.io.IOException;
import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Place where a sync of some kind of processing queue can send its data to (if we are in some kind of vectored queue
 * mode.
 */
public interface Sink<T> extends AutoCloseable, JsonInitable, Consumer<T> {
    boolean init(JsonNode node);

    boolean start() throws IOException;

    default Sink<T> maxPerTransaction(long max) {
        return new MaxItemsPerTransactionSink<>(this, max);
    }

    default Sink<T> filter(Predicate<T> predicate) {
        return new PredicatedSink(this, predicate);
    }

    default <I> Sink<I> map(Function<I, T> function) {
        return new MappingSink(this, function);
    }

    default Sink<T> tee(Sink<T> other) {
        return new TeeSink<>(this, other);
    }

    /**
     * Make consumer friendly
     *
     * @param t
     */
    default void accept(T t) {
        try {
            add(t);
        } catch (IOException | StoreException e) {
            throw new RuntimeException("Sink.accept failed: " + e.getMessage(), e);
        }
    }

    boolean add(T o) throws IOException, StoreException;

    default boolean addAll(final Collection<T> oList) throws IOException, StoreException {
        boolean success = true;
        for (T o : oList) {
            if (!add(o)) {
                success = false;
            }
        }
        return success;
    }

    boolean stop() throws IOException;

    void close() throws IOException;

    default Sink<T> merge(Sink<T> in) {
        return in;
    }
}
