package ht.util.io.largedata.iterator;

import java.util.Comparator;
import java.util.Iterator;

/**
 * Copyright (c) 2003 - present HiTorro All rights reserved. User: chris Date: Jan 31, 2005 Time: 6:10:37 PM
 * <p/>
 * Similar to a Selection tree in that it takes two iterators, in this case though the intent is to provide an iterator
 * of values that we want and a stop iterator of values that we dont want, it assumes iterable values are both in the
 * same sort order.
 * <p/>
 * Assuming the iterators provide their values in the same order as the <E>.compareTo method would order them, we
 * construct an iterator that uses one iterator to stop out elements of another.
 * <p/>
 * e.g.
 * <p/>
 * iter    stop ====    ==== a        a b        e c d e
 * <p/>
 * will apply: ============= b c d
 *
 * @author ccollins
 */
public class FilteringSelectionTreeIterator<E, C extends Comparator<E>> implements Iterator<E> {

    private Iterator<E> m_iter;
    private boolean m_iterHasElement = true;
    private Iterator<E> m_stop;
    private boolean m_stopHasElement = true;

    private E m_returnMe = null;

    private E m_iterComparable = null;
    private E m_stopComparable = null;
    private C m_comparitor;

    /**
     * @param iter the iterator of values we care to filter
     * @param stop the iterator of values we want to stop in the iter iterator if there is a test.
     */
    public FilteringSelectionTreeIterator(Iterator<E> iter, Iterator<E> stop, C comparitor) {
        m_iter = iter;
        m_stop = stop;
        m_comparitor = comparitor;
        getIter();
        getStop();
        m_returnMe = getIterValAndBackFill();
    }

    public boolean hasNext() {
        return m_returnMe != null;
    }

    public E next() {
        // TODO Auto-generated method stub
        E returnMeNow = m_returnMe;
        m_returnMe = getIterValAndBackFill();
        return returnMeNow;
    }

    public void remove() {
        // not implemented
    }

    protected final E getIterValAndBackFill() {
        while (true) {
            E val = getIterValAndBackFillAux();
            if (val != null) {
                return val;
            }
            if (m_iterHasElement == false) {
                return null;
            }
        }
    }

    protected final E getIterValAndBackFillAux() {
        if (m_iterHasElement && m_stopHasElement) {
            int val = m_comparitor.compare(m_iterComparable, m_stopComparable);
            if (val == 0) {
                // must remove iter val and try again...advance iter but dont return
                // the value
                getIter();
                return null;
            } else if (val > 0) {
                // stop is smaller so lets move through the stops but dont return anything
                // because we have to try again with another stop
                getStop();
                return null;
            } else {
                // stop is larger so we dont have an iterator test....we can
                // return this iterators value.
                return getIter();
            }

        }
        if (m_iterHasElement) {
            return getIter();
        }
        // the iterator does not have any more
        m_iterHasElement = false;
        return null;
    }

    private final E getIter() {
        E returnMe = m_iterComparable;
        if (m_iter.hasNext()) {
            m_iterComparable = m_iter.next();
        } else {
            m_iterHasElement = false;
        }
        return returnMe;
    }

    private final E getStop() {
        E returnMe = m_stopComparable;
        if (m_stop.hasNext()) {
            m_stopComparable = m_stop.next();
        } else {
            m_stopHasElement = false;
        }
        return returnMe;
    }
}

