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

import com.hitorro.util.core.Log;
import com.hitorro.util.core.iterator.AbstractIterator;
import com.hitorro.util.core.string.Fmt;

import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

/**
 * <p/>
 * Based upon a selection tree model for merging n streams of data, the SelectionTreeIterator is given two Iterators
 * that provide elements that are Comparable and provide the smallest value.  These iterators are built into a tree of
 * iterators that provide n channels to be merged.
 * <p/>
 * see the following for a description:
 * <p/>
 * Log2P Comparisons are necessary for a selection within the tree.
 * <p/>
 * http://www.fmi.uni-passau.de/~fickensc/Seminars/Proseminar_External_Sorting_(Folien).pdf
 */
public class SelectionTreeIterator<E, C extends Comparator<E>>
        extends AbstractIterator<E> {
    private Iterator<E> m_left;
    private boolean m_leftHasElement = true;
    private Iterator<E> m_right;
    private boolean m_rightHasElement = true;

    private E m_leftComparable = null;
    private E m_rightComparable = null;
    private C m_comparitor;

    public SelectionTreeIterator(Iterator<E> left, Iterator<E> right, C comparitor) {
        initSelf(left, right, comparitor);
    }

    /**
     * Take n iterators as long as they are of the same type and construct a tree of selection tree iterators to handle
     * the n way apply
     *
     * @param iters
     */
    public SelectionTreeIterator(C comparator, Iterator<E>... iters) {
        initArray(comparator, iters);
    }

    public boolean hasNext() {
        return m_leftHasElement || m_rightHasElement;
    }

    public E next() {
        return getSmallestAndBackFill();
    }

    public void remove() {
        // not implemented
    }

    protected final E getSmallestAndBackFill() {
        if (m_leftHasElement && m_rightHasElement) {
            int compareVal = m_comparitor.compare(m_leftComparable, m_rightComparable);
            if (compareVal >= 0) {
                return getRight();
            } else {
                return getLeft();
            }
        }
        if (m_leftHasElement) {
            return getLeft();
        }
        return getRight();
    }

    private final E getLeft() {
        E returnMe = m_leftComparable;
        if (m_left.hasNext()) {
            m_leftComparable = m_left.next();
            if (m_leftComparable == null) {
                // paranoid
                Log.util.error("SelectTreeIterator left channel returned null hasNext == true %s", this.toString());
                m_leftHasElement = false;
            }
        } else {
            m_leftHasElement = false;
        }
        return returnMe;
    }

    private final E getRight() {
        E returnMe = m_rightComparable;
        if (m_right.hasNext()) {
            m_rightComparable = m_right.next();
            if (m_rightComparable == null) {
                // paranoid
                Log.util.error("SelectTreeIterator right channel returned null hasNext == true %s", this.toString());
                m_rightHasElement = false;
            }
        } else {
            m_rightHasElement = false;
        }
        return returnMe;
    }

    public String toString() {
        return Fmt.S("ST(l: %s, r: %s)", m_left.toString(), m_right.toString());
    }

    /**
     * MUST be > 1 element
     *
     * @param comparator
     * @param iters
     * @return
     */
    private boolean initArray(C comparator, Iterator<E> iters[]) {
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
                        nextQueue.add(new SelectionTreeIterator<E, C>(queue.remove(), queue.remove(), comparator));
                    } else {
                        nextQueue.add(queue.remove());
                    }

                    queue = nextQueue;
                    nextQueue = new LinkedList<Iterator<E>>();
                    continue;
                }
            } else if (s == 2) {
                // no more nextS
                return initSelf(queue.remove(), queue.remove(), comparator);
            }
            nextQueue.add(new SelectionTreeIterator<E, C>(queue.remove(), queue.remove(), comparator));

        }
        return initSelf(queue.remove(), queue.remove(), comparator);
    }

    private boolean initSelf(Iterator<E> left, Iterator<E> right, C comparitor) {
        m_left = left;
        m_right = right;
        m_comparitor = comparitor;
        getLeft();
        getRight();
        return true;
    }

    public void close() throws Exception {
        if (m_left != null && m_left instanceof AutoCloseable) {
            ((AutoCloseable) m_left).close();
        }

        if (m_right != null && m_right instanceof AutoCloseable) {
            ((AutoCloseable) m_right).close();
        }
    }
}

