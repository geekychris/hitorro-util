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
import com.hitorro.util.core.iterator.Mapper;
import com.hitorro.util.core.queue.ThreadedQueue;
import com.hitorro.util.io.StoreException;

import java.io.IOException;

/**
 *
 */
public abstract class BaseSink<E> implements Sink<E> {
    /**
     * Create an asynchronous put.
     *
     * @param sink
     * @param queueLength
     * @param sendCompleteOnStop
     * @param name
     * @return
     */
    public static <E> ThreadedQueueSink<E> async(Sink<E> sink, int queueLength, boolean sendCompleteOnStop,
                                                 String name, boolean startThread) {
        ThreadedQueue tq = new ThreadedQueue(queueLength);
        ThreadedQueueSink tqs = new ThreadedQueueSink(tq, sendCompleteOnStop);
        SinkFromThreadedQueue dest = new SinkFromThreadedQueue(tq, sink, name, startThread);
        tqs.setPeer(dest);
        return tqs;
    }

    public abstract boolean init(JsonNode map);

    @Override
    public abstract boolean start() throws IOException;

    @Override
    public abstract boolean add(final E o) throws IOException, StoreException;

    @Override
    public abstract boolean stop() throws IOException;

    /**
     * We wrapper this Sink with a target of E with a mapper that is given (I) This works backwards from iterators, from
     * target item encapsulating to whatever ultimately calls the put
     *
     * @param mapper
     * @param <O>
     * @return
     */
    public <O> MappingSink<O, E> map(Mapper<O, E> mapper) {
        return new MappingSink<O, E>(this, mapper);
    }

    /**
     * Create an asynchronous put.  If you dont want to use a simple thread, then you can pass false for the threadstart
     * and then grab the peer from the returned object so you can pass the runnable to whatever mechanism
     *
     * @param queueLength
     * @param sendCompleteOnStop
     * @param name
     * @return
     */
    public ThreadedQueueSink<E> async(int queueLength, boolean sendCompleteOnStop, String name, boolean startThread) {
        return BaseSink.async(this, queueLength, sendCompleteOnStop, name, startThread);
    }

    @Override
    public void close() throws IOException {
        // do nothing
    }

}
