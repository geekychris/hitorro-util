package ht.util.core.iterator;

import java.util.Iterator;

/**
 * Copyright (c) 2003 - present HiTorro All rights reserved. User: chris Date: Oct 18, 2006 Time: 1:40:46 PM
 */
public class PushbackIterator<E> extends AbstractIterator<E> {
    private int m_bufferSize;
    private Iterator<E> m_iter;
    private E[] m_buffer;
    private int m_pushedBack = 0;
    private long m_currentNextFreeSlot = 0;

    private long m_markPosition = 0;
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
        if (m_pushedBack > 0) {
            return true;
        }
        return m_iter.hasNext();
    }

    public E next() {
        if (m_pushedBack > 0) {
            return getObjectFromBuffer();
        }
        return getObjectFromIterator();
    }

    private E getObjectFromBuffer() {
        E o = m_buffer[(int) (m_currentNextFreeSlot - (m_pushedBack)) % m_bufferSize];

        m_pushedBack--;
        return o;
    }

    private E getObjectFromIterator() {
        E temp = m_iter.next();
        m_buffer[(int) m_currentNextFreeSlot % m_bufferSize] = temp;
        m_currentNextFreeSlot++;

        return temp;
    }

    public void remove() {
    }

    public void mark() {
        if (m_pushedBack > 0) {
            m_markPosition = m_currentNextFreeSlot - m_pushedBack;
        } else {
            m_markPosition = m_currentNextFreeSlot;
        }
        m_marked = true;
    }

    public void reset() {
        if (m_marked) {
            pushBack((int) (m_currentNextFreeSlot - m_markPosition));
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
        m_pushedBack = nObjects;
    }

    @Override
    public void close() throws Exception {
        close(m_iter);
    }
}
