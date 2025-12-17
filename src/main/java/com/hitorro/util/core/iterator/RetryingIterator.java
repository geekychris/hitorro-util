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
package com.hitorro.util.core.iterator;

import com.hitorro.util.core.Env;

import java.util.Iterator;

/**
 * Iterator responsible for retrying indefinitely the next method of an underlying iterator.  Used with iterators that
 * may temporarily start out of data, but are possibly recoverable.
 */
public class RetryingIterator<E> extends AbstractIterator<E> {
    private Iterator<E> victim;
    private int sleep;
    private boolean running = true;

    public RetryingIterator(Iterator<E> victim, int sleep) {
        this.victim = victim;
        this.sleep = sleep;
    }

    public void close() throws Exception {
        if (victim instanceof AbstractIterator) {
            ((AbstractIterator) victim).close();
        }
    }

    public void stop() {
        running = false;
    }

    public boolean hasNext() {
        while (running && !victim.hasNext()) {
            Env.sleepNSeconds(sleep);
        }
        return running;
    }

    public E next() {
        return victim.next();
    }

    public void remove() {
        victim.remove();
    }
}

