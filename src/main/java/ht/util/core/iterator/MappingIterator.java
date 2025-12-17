package ht.util.core.iterator;

import ht.util.core.iterator.mappers.BaseMapper;

import java.util.Iterator;
import java.util.function.Function;

/**
 * Copyright (c) 2003 - present HiTorro All rights reserved.
 * <p/>
 * User: chris
 * <p/>
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
