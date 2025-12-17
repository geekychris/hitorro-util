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

import com.hitorro.util.core.Log;

import java.util.Iterator;
import java.util.concurrent.atomic.AtomicLong;

public class CountingIterator<E> extends Iterator2AbstractIterator<E> {

    private AtomicLong aLong;
    private long reportPeriodically = 0;
    private long prevTime = System.currentTimeMillis();

    public CountingIterator(Iterator<E> iter, AtomicLong aLong, long reportPeriodically) {
        super(iter);
        this.aLong = aLong;
        this.reportPeriodically = reportPeriodically;

    }

    @Override
    public E next() {
        aLong.incrementAndGet();
        if (reportPeriodically != 0 && aLong.get() % reportPeriodically == 0) {
            long n = System.currentTimeMillis();
            long d = n - prevTime;
            if (d > 1000) {
                d = d / 1000;
                long itemsPerSec = reportPeriodically / d;
                Log.util.info("Count %s, items %s per second", aLong.get(), itemsPerSec);
            } else {
                long itemsPerMilli = reportPeriodically / d;
                Log.util.info("Count %s, items %s per milli", aLong.get(), itemsPerMilli);
            }
            prevTime = n;

        }
        return super.next();
    }
}