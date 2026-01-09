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
package com.hitorro.util.io.largedata.iterator;

import java.util.Comparator;
import java.util.Iterator;

/**
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
    private boolean iterHasElement = true;
    private Iterator<E> m_stop;
    private boolean stopHasElement = true;

    private E m_returnMe = null;

    private E iterComparable = null;
    private E stopComparable = null;
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
            if (iterHasElement == false) {
                return null;
            }
        }
    }

    protected final E getIterValAndBackFillAux() {
        if (iterHasElement && stopHasElement) {
            int val = m_comparitor.compare(iterComparable, stopComparable);
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
        if (iterHasElement) {
            return getIter();
        }
        // the iterator does not have any more
        iterHasElement = false;
        return null;
    }

    private final E getIter() {
        E returnMe = iterComparable;
        if (m_iter.hasNext()) {
            iterComparable = m_iter.next();
        } else {
            iterHasElement = false;
        }
        return returnMe;
    }

    private final E getStop() {
        E returnMe = stopComparable;
        if (m_stop.hasNext()) {
            stopComparable = m_stop.next();
        } else {
            stopHasElement = false;
        }
        return returnMe;
    }
}

