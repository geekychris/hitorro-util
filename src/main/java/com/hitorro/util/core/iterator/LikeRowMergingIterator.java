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

import java.io.IOException;
import java.util.Comparator;
import java.util.Iterator;
import java.util.function.BiFunction;

/**
 */

public class LikeRowMergingIterator<E> extends AbstractIterator<E> {
    private Iterator<E> m_iter;

    private E newerVal;
    private Comparator<E> m_comparator;
    private BiFunction<E, E, E> m_merger;
    private boolean initialized = false;

    public LikeRowMergingIterator(Iterator<E> iter, Comparator<E> comparator, BiFunction<E, E, E> merger) {
        m_merger = merger;
        m_comparator = comparator;
        m_iter = iter;

    }

    private void init() {

        if (m_iter.hasNext()) {
            newerVal = m_iter.next();
        }
        initialized = true;
    }

    public boolean hasNext() {
        if (!initialized) {
            init();
        }
        return newerVal != null;
    }

    public E next() {
        E older = newerVal;
        newerVal = null;
        while (m_iter.hasNext()) {
            // where we have another value to read
            newerVal = m_iter.next();
            if (m_comparator.compare(older, newerVal) == 0) {
                older = m_merger.apply(older, newerVal);
            } else {
                // not same
                return older;
            }
        }
        if (!m_iter.hasNext()) {
            newerVal = null;
        }
        return older;
    }

    public void remove() {
        // Not implemented

    }

    @Override
    public void close() throws IOException {
    }
}
