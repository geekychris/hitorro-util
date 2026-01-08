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

import com.hitorro.util.core.iterator.mappers.BaseMapper;

import java.util.Iterator;
import java.util.function.Function;

/**

 * Given an iterator of type F, spews out results of type E If the me returns a null, then we keep reading the
 * iterator until we find a non null mapping
 */
public class MappingIterator<F, E> extends AbstractIterator<E> {
    private Iterator<F> m_iter;
    private Function<F, E> m_map;
    private E targetElem = null;

    public MappingIterator(Iterator<F> iter, Function<F, E> map) {
        m_iter = iter;
        m_map = map;
        readNext();
    }

    private void readNext() {
        while (m_iter.hasNext()) {
            targetElem = m_map.apply(m_iter.next());
            if (targetElem != null) {
                return;
            }
        }
        targetElem = null;
    }

    public boolean hasNext() {
        return targetElem != null;
    }

    public E next() {
        E ret = targetElem;
        readNext();
        return ret;
    }

    public void remove() {
        m_iter.remove();
    }

    @Override
    public void close() throws Exception {
        boolean flag = close(m_iter);
        if (m_map instanceof BaseMapper) {
            ((BaseMapper) m_map).close();
        }
    }
}
