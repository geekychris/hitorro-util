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
import com.hitorro.util.core.Log;
import com.hitorro.util.core.queue.ThreadedQueue;
import com.hitorro.util.core.queue.ThreadedQueueCanceledException;
import com.hitorro.util.core.queue.ThreadedQueueTimeoutException;
import com.hitorro.util.io.StoreException;

import java.io.IOException;

/**
 * Encapsulates a queue so that sinking can be asynchronous
 */
public class ThreadedQueueSink<E> extends BaseSink<E> {
    private ThreadedQueue<E> queue;
    private boolean completeQueueOnStop;
    private SinkPeerInterface peer;

    public ThreadedQueueSink(ThreadedQueue<E> queue, boolean completeQueueOnStop) {
        this.queue = queue;
        this.completeQueueOnStop = completeQueueOnStop;
    }

    public boolean peerCompleted() {
        if (peer != null) {
            return peer.completed();
        }
        return false;
    }

    /**
     * Possible the other end of this put is the SinkFromThreadedQueue, we need to keep track of that guy in the case it
     * was constructed via
     *
     * @param peer
     */
    void setPeer(SinkPeerInterface peer) {
        this.peer = peer;
    }

    @Override
    public boolean init(JsonNode node) {
        return true;
    }

    @Override
    public boolean start() {
        return true;
    }

    @Override
    public boolean add(final E o) throws IOException, StoreException {
        try {
            queue.put(o);
        } catch (ThreadedQueueCanceledException e) {
            Log.queue.error("Unable to put to put %s %e", e, e);
            return false;
        } catch (ThreadedQueueTimeoutException e) {
            Log.queue.error("Unable to put to put %s %e", e, e);
            return false;
        }
        return true;
    }

    @Override
    public boolean stop() throws IOException {
        if (completeQueueOnStop) {
            queue.setQueueComplete();
        }
        return true;
    }
}
