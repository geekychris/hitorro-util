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

/**
 */
public class PushbackIterator<E> extends AbstractIterator<E> {
    private int m_bufferSize;
    private Iterator<E> m_iter;
    private E[] m_buffer;
    private int pushedBack = 0;
    private long currentNextFreeSlot = 0;

    private long markPosition = 0;
    private boolean m_marked = false;

    private PushbackIterator() {
    }

    @SuppressWarnings("unchecked")
    public PushbackIterator(Iterator<E> iterIn, int bufferSize) {
        m_bufferSize = bufferSize;
        m_iter = iterIn;
        m_buffer = (E[]) new Object[m_bufferSize];
    }

    public boolean hasNext() {
        if (pushedBack > 0) {
            return true;
        }
        return m_iter.hasNext();
    }

    public E next() {
        if (pushedBack > 0) {
            return getObjectFromBuffer();
        }
        return getObjectFromIterator();
    }

    private E getObjectFromBuffer() {
        E o = m_buffer[(int) (currentNextFreeSlot - (pushedBack)) % m_bufferSize];

        pushedBack--;
        return o;
    }

    private E getObjectFromIterator() {
        E temp = m_iter.next();
        m_buffer[(int) currentNextFreeSlot % m_bufferSize] = temp;
        currentNextFreeSlot++;

        return temp;
    }

    public void remove() {
    }

    public void mark() {
        if (pushedBack > 0) {
            markPosition = currentNextFreeSlot - pushedBack;
        } else {
            markPosition = currentNextFreeSlot;
        }
        m_marked = true;
    }

    public void reset() {
        if (m_marked) {
            pushBack((int) (currentNextFreeSlot - markPosition));
            m_marked = false;
        }
    }

    public void markIfNotAlreadyMarked() {
        if (m_marked == false) {
            mark();
        }
    }

    public void pushBack(int nObjects) {
        if (nObjects > m_bufferSize) {
            throw new ArrayIndexOutOfBoundsException();
        }
        pushedBack = nObjects;
    }

    @Override
    public void close() throws Exception {
        close(m_iter);
    }
}
