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

import java.util.HashMap;
import java.util.Map;

/**
 * Keeps a queue of items taken from an iterator.  The consumer of the elements may have these elements in use for some
 * time.  For example, these elements could be files of documents, each element may be given to a separate process or
 * thread it maybe the case that one thread hangs or is very slow. We may not wish processing to continue while this one
 * item is "taking too long".
 * <p/>
 * We could measure the amount of time that an element takes to process, which would require prior heuristics or require
 * an amount of time to stabilize.  Another approach is to provide a sliding window of elements that are in play, if an
 * element is in play beyond a max window size we could prevent more elements from being provided.
 * <p/>
 * What is currently missing is any "alarm bells" mechanism that would alert some handler that it simply is taking way
 * too long.
 */
public class QueueScoreBoard<E> {
    private int seqNumber = 0;
    private QueueEntry<E> newestEntry = null;
    private QueueEntry<E> oldestEntry = null;
    private Map<E, QueueEntry> map = new HashMap();
    private AbstractIterator<E> dvi;

    public QueueScoreBoard(AbstractIterator<E> dvi) {
        this.dvi = dvi;
    }

    public int getSeqNumber() {
        return seqNumber++;
    }

    private QueueEntry getNextEntry() {
        if (oldestEntry != null) {
            // examine the distance
        }
        if (dvi.hasNext()) {
            E e = dvi.next();
            QueueEntry qfe = new QueueEntry(e, getSeqNumber());
            newestEntry = qfe;
            return qfe;
        } else {
            return null;
        }
    }

    /**
     * remove the freed up element from the listFiles
     *
     * @param e
     */
    public void free(E e) {
        QueueEntry<E> qe = map.remove(e);
        if (qe == null) {
            // XXX null entry????
            return;
        }
        QueueEntry<E> n = qe.getNext();
        QueueEntry<E> p = qe.getPrior();
        if (n != null) {
            if (p != null) {
                // item is in the middle
                n.setPrior(p);
                p.setNext(n);
            } else {
                n.setPrior(null);
                // its now the tail
                oldestEntry = n;
            }
        } else {
            if (p != null) {
                // item is at head
                p.setNext(null);
                this.newestEntry = p;
            }
        }
    }
}

class QueueEntry<E> {
    private QueueEntry<E> prior;
    private QueueEntry<E> next;
    private E entry;
    private int sequenceNumber;
    private long takenAt;

    QueueEntry(E entry, int sequenceNumber) {
        this.entry = entry;
        this.sequenceNumber = sequenceNumber;
        takenAt = System.currentTimeMillis();
    }

    public int hashCode() {
        return entry.hashCode();
    }

    public boolean equals(Object o) {
        return entry.equals(o);
    }

    public long getTakenTime() {
        return takenAt;
    }

    public QueueEntry<E> getPrior() {
        return prior;
    }

    public void setPrior(QueueEntry<E> prior) {
        this.prior = prior;
    }

    public QueueEntry<E> getNext() {
        return next;
    }

    public void setNext(QueueEntry<E> next) {
        this.next = next;
    }

    public int getSequenceNumber() {
        return sequenceNumber;
    }
}
