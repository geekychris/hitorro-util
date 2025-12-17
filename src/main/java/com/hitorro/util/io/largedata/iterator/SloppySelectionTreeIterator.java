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

import com.hitorro.util.core.Constants;
import com.hitorro.util.core.iterator.CloseableIterator;
import com.hitorro.util.core.string.Fmt;

import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

/**
 * An adaptation of a select tree that handled left or right being NULL, but with the assumption that the channel will
 * "rejuvinate" and potentially give us more items later.
 * <p/>
 * <p/>
 * see the following for a description:
 * <p/>
 * Log2P Comparisons are necessary for a selection within the tree.
 * <p/>
 * http://www.fmi.uni-passau.de/~fickensc/Seminars/Proseminar_External_Sorting_(Folien).pdf
 */
public class SloppySelectionTreeIterator<E, C extends Comparator<E>>
        implements Iterator<E>, CloseableIterator<E> {
    private SloppyIterContainer<E> m_left;
    private SloppyIterContainer<E> m_right;

    private C m_comparitor;

    public SloppySelectionTreeIterator(Iterator<E> left, Iterator<E> right, int secondsDelay, C comparitor) {
        initSelf(left, right, comparitor, secondsDelay);
    }

    /**
     * Take n iterators as long as they are of the same type and construct a tree of selection tree iterators to handle
     * the n way apply
     *
     * @param iters
     */
    public SloppySelectionTreeIterator(C comparator, int secondsDelay, Iterator<E>... iters) {
        initArray(comparator, secondsDelay, iters);
    }

    public boolean hasNext() {
        return m_left.hasNext() || m_right.hasNext();
    }

    public E next() {
        return getSmallestAndBackFill();
    }

    public void remove() {
        // not implemented
    }

    protected final E getSmallestAndBackFill() {

        if (m_left.hasNext() && m_right.hasNext()) {
            int compareVal = m_comparitor.compare(m_left.getCurrent(), m_right.getCurrent());
            if (compareVal <= 0) {
                return m_left.next();
            } else {
                return m_right.next();
            }
        }
        if (m_left.hasNext()) {
            return m_left.next();
        }
        if (m_left.hasNext()) {
            return m_right.next();
        }
        return null;
    }

    public String toString() {
        return Fmt.S("SST(l: %s, r: %s)", m_left.toString(), m_right.toString());
    }

    /**
     * MUST be > 1 element
     *
     * @param comparator
     * @param iters
     * @return
     */
    private boolean initArray(C comparator, int secondsDelay, Iterator<E> iters[]) {
        Queue<Iterator<E>> queue = new LinkedList<Iterator<E>>();

        for (Iterator<E> i : iters) {
            queue.add(i);
        }
        Queue<Iterator<E>> nextQueue = new LinkedList<Iterator<E>>();
        while (!(queue.size() == 2 && nextQueue.size() == 0)) {
            int s = queue.size();
            int nextS = nextQueue.size();
            if (s == 0) {
                queue = nextQueue;
                nextQueue = new LinkedList<Iterator<E>>();
                continue;
            } else if (nextS > 0) {
                if (s == 1 || s == 2) {
                    if (s == 2) {
                        nextQueue.add(new SloppySelectionTreeIterator<E, C>(queue.remove(), queue.remove(),
                                secondsDelay, comparator));
                    } else {
                        nextQueue.add(queue.remove());
                    }

                    queue = nextQueue;
                    nextQueue = new LinkedList<Iterator<E>>();
                    continue;
                }
            } else if (s == 2) {
                // no more nextS
                return initSelf(queue.remove(), queue.remove(), comparator, secondsDelay);
            }
            nextQueue.add(new SloppySelectionTreeIterator<E, C>(queue.remove(), queue.remove(), secondsDelay, comparator));
        }
        return initSelf(queue.remove(), queue.remove(), comparator, secondsDelay);
    }

    private boolean initSelf(Iterator<E> left, Iterator<E> right, C comparitor, int secondsDelay) {
        m_left = new SloppyIterContainer(left, secondsDelay);
        m_right = new SloppyIterContainer(right, secondsDelay);
        m_comparitor = comparitor;
        return true;
    }

    public void close() throws Exception {
        if (m_left != null && m_left instanceof AutoCloseable) {
            m_left.close();
        }

        if (m_right != null && m_right instanceof AutoCloseable) {
            m_right.close();
        }
    }
}

class SloppyIterContainer<T> {
    private long nextPollTime = -1;
    private long pollGoverner = Constants.MillisInSecond * 10;
    private Iterator<T> iter;
    private T obj = null;


    SloppyIterContainer(Iterator<T> iter, int secondsDelay) {
        this.iter = iter;
        if (iter instanceof SloppySelectionTreeIterator) {
            // if the child is not some real iterator then we should not pause on it.
            // thats because we dont want to propogate pauses all the way up the tree
            pollGoverner = 0;
        } else {
            pollGoverner = Constants.MillisInSecond * secondsDelay;
        }
    }

    public void close() throws Exception {
        if (iter != null && iter instanceof AutoCloseable) {
            ((AutoCloseable) iter).close();
        }
    }

    public T getCurrent() {
        return obj;
    }

    public T next() {
        T ret = obj;
        if (iter.hasNext()) {
            obj = iter.next();
            if (pollGoverner != 0) {
                nextPollTime = System.currentTimeMillis() + pollGoverner;
            }
        } else {
            obj = null;
        }
        return ret;
    }

    public boolean hasNext() {
        if (obj != null) {
            return true;
        }
        if (pollGoverner == 0) {
            return getNextAux();
        } else {
            long curr = System.currentTimeMillis();
            if (curr < nextPollTime || nextPollTime == -1) {
                nextPollTime = curr + pollGoverner;
                return getNextAux();
            }
        }
        return false;
    }

    private boolean getNextAux() {
        if (iter.hasNext()) {
            obj = iter.next();
            return obj != null;
        }
        return false;
    }
}

