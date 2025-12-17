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

import com.hitorro.util.core.Constants;
import com.hitorro.util.core.Env;

import java.io.IOException;
import java.util.Iterator;

/**
 * Prevents a consumer from consuming content beyond a certain rpm.
 */
public class RateLimitingIterator<E> extends AbstractIterator<E> {
    private Iterator<E> iter;
    private int rpm;
    private long offset;
    private long start;

    public RateLimitingIterator(int rpm, Iterator<E> iterator) {
        this.iter = iterator;
        this.rpm = rpm;
        this.offset = Constants.MillisInMinute / rpm;
        this.start = System.currentTimeMillis() - offset;
    }

    @Override
    public void close() throws IOException {
        if (iter instanceof CloseableIterator) {
            try {
                AbstractIterator.attemptClose(iter);
            } catch (Exception e) {
                return;
            }
        }
    }

    public boolean hasNext() {
        long delta = offset - (System.currentTimeMillis() - start);

        if (delta > 0) {
            Env.sleepMillis(delta);
        }
        start = System.currentTimeMillis();
        return iter.hasNext();
    }

    public E next() {
        E ret = iter.next();
        return ret;
    }

    public void remove() {
        iter.remove();
    }
}
