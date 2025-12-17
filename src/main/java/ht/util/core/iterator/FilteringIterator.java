package ht.util.core.iterator;

import java.util.Iterator;
import java.util.function.Predicate;

/**
 * Copyright (c) 2003 - present HiTorro All rights reserved.
 * <p/>
 * User: chris provide a filtering mechanism to iterating through a set of results
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
