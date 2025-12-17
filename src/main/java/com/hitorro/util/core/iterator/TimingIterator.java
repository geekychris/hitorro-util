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

import com.hitorro.util.servicecounters.CounterService;
import com.hitorro.util.servicecounters.CounterSet;
import com.hitorro.util.servicecounters.registers.DoubleDivideRegister;
import com.hitorro.util.servicecounters.registers.LongRegister;

import java.util.Iterator;

/**
 *
 */
public class TimingIterator<T> extends AbstractIterator<T> {
    private Iterator<T> iter;
    private CounterSet cs;
    private LongRegister callCount;
    private LongRegister accumulativeTime;
    private DoubleDivideRegister avgTime;
    private long time;

    public TimingIterator(CounterSet cs, Iterator<T> iterator) {
        this.iter = iterator;
        this.cs = cs;
        callCount = cs.getLongRegister("callcount", "Callcount");
        accumulativeTime = cs.getLongRegister("accumtime", "Accumulative time");
        avgTime = cs.getDoubleDivideRegister("avgTime", "Average time", accumulativeTime, callCount);
        cs.finishInit(CounterService.getService().getCounterContext());

    }

    @Override
    public void close() throws Exception {
        if (iter instanceof AutoCloseable) {
            ((AutoCloseable) iter).close();
        }
    }

    @Override
    public boolean hasNext() {
        time = System.currentTimeMillis();
        boolean ret = iter.hasNext();
        accumulativeTime.incrementBy(System.currentTimeMillis() - time);
        return ret;
    }

    @Override
    public T next() {
        time = System.currentTimeMillis();
        T ret = iter.next();
        accumulativeTime.incrementBy(System.currentTimeMillis() - time);
        callCount.incrementBy(1);
        return ret;
    }

    @Override
    public void remove() {
        iter.remove();
    }
}
