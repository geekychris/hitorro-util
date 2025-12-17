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
import com.hitorro.util.core.opers.HTPredicate;
import com.hitorro.util.core.opers.LogicalSelector;
import com.hitorro.util.io.StoreException;

import java.io.IOException;

/**
 * Sink that has a set of sinks.  Sink to this it decides which sub put to goto. Acts as a wrapper, uses simple logical
 * constraints to decide which path.  If there are too many possible outcomes a better algorithm should probably be
 * chosen as the LogicalSelector currently just scans a listFiles of logical operators
 */
public class SplitSink<I> extends BaseSink<I> {
    protected LogicalSelector<I, Sink<I>> ts = new LogicalSelector();
    private boolean failOnMissingSink = false;

    public SplitSink(boolean failOnMissingSink) {
        this.failOnMissingSink = failOnMissingSink;
    }

    public void add(HTPredicate<I> oper, Sink<I> sink) {
        ts.addSelection(oper, sink);
    }

    @Override
    public boolean init(JsonNode node) {
        boolean success = true;
        for (Sink<I> s : ts.getTargets()) {
            if (!s.init(node)) {
                success = false;
            }
        }
        return success;
    }

    @Override
    public boolean start() {
        ts.finishSetup();
        return true;
    }

    @Override
    public boolean add(final I o) throws IOException, StoreException {
        Sink<I> sink = ts.select(o);
        if (sink != null) {
            return sink.add(o);
        }
        return !failOnMissingSink;
    }

    @Override
    public boolean stop() throws IOException {
        boolean success = true;
        for (Sink<I> s : ts.getTargets()) {
            if (!s.stop()) {
                success = false;
            }
        }
        return success;
    }
}
