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

import java.util.Iterator;
import java.util.function.Predicate;

/**
 provide a filtering mechanism to iterating through a set of results
 */
public class FilteringIterator<E> extends AbstractIterator<E> {
    private Iterator<E> m_iter;
    private Predicate<E> m_oper;
    private E m_next;

    public FilteringIterator(Iterator<E> iter, Predicate<E> oper) {
        m_iter = iter;
        m_oper = oper;
        m_next = null;
    }

    public boolean hasNext() {
        if (m_next != null) {
            // just in case someone calls hasNext more than once.
            return true;
        }
        m_next = nextAux();
        return m_next != null;
    }

    public E next() {
        E returnMe = m_next;
        m_next = null;
        return returnMe;
    }

    public void remove() {
        m_iter.remove();
    }

    private E nextAux() {
        while (m_iter.hasNext()) {
            E next = m_iter.next();
            if (m_oper.test(next)) {
                return next;
            }
        }
        return null;
    }

    @Override
    public void close() throws Exception {
        close(m_iter);
    }
}
