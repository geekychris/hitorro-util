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
import java.util.Iterator;

/**
 *
 */
public class NullRemovingIterator<E> extends AbstractIterator<E> {
    private Iterator<E> t;
    private boolean first = true;

    private E e = null;

    public NullRemovingIterator(Iterator<E> t) {
        this.t = t;
    }

    public boolean hasNext() {
        if (first) {
            first = false;
            advanceTillNotNull();
        }
        return e != null;
    }

    public E next() {
        first = true;
        return e;
    }

    public void remove() {
        t.remove();
    }

    @Override
    public void close() throws IOException {
        try {
            if (t instanceof AutoCloseable) {
                ((AutoCloseable) t).close();
            }
        } catch (Exception e1) {
            return;
        }
    }

    private void advanceTillNotNull() {
        e = null;
        while (t.hasNext()) {
            e = t.next();
            if (e != null) {
                return;
            }
        }
    }
}
