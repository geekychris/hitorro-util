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
import com.hitorro.util.core.iterator.sinks.Sink;
import com.hitorro.util.io.StoreException;

import java.io.IOException;
import java.util.Iterator;
import java.util.function.Predicate;

public class FilterToSinkIterator<T> extends AbstractIterator<T> {
    protected Sink<T> sink;
    private Predicate<T> filter;
    private Iterator<T> inIter;

    public FilterToSinkIterator(Iterator<T> inIter, Predicate<T> filter, Sink<T> sink) {
        this.filter = filter;
        this.sink = sink;
        this.inIter = inIter;
    }

    public boolean hasNext() {
        return inIter.hasNext();
    }

    public T next() {
        T tmp = inIter.next();
        if (filter.test(tmp)) {
            try {
                sink.add(tmp);
            } catch (IOException e) {
                Log.util.error("FilterToSinkIterator: IOException during sink: %s", e.getMessage());
            } catch (StoreException e) {
                Log.util.error("FilterToSinkIterator: StoreException during sink: %s", e.getMessage());
            }
        }
        return tmp;
    }

    public void remove() {
        inIter.remove();
    }

    @Override
    public void close() throws Exception {
        if (inIter instanceof AutoCloseable) {
            ((AutoCloseable) inIter).close();
        }
    }
}
