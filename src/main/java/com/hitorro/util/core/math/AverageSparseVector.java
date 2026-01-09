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
package com.hitorro.util.core.math;


import gnu.trove.iterator.TLongDoubleIterator;
import gnu.trove.map.hash.TLongDoubleHashMap;

/**
 Given a series of sparse vectors, generate a sparseVector which is the average of all (that is any
 * element ei = sum(sv1.ei, sv2.ei, ....) / number of sparse vectors
 */
public class AverageSparseVector {
    private TLongDoubleHashMap m_map = new TLongDoubleHashMap();
    private int m_vectors = 0;

    public void reset() {
        m_map.clear();
        m_vectors = 0;
    }

    public void add(SparseVector v1) {
        for (int i = 0; i < v1.elementCount; i++) {
            long pos = v1.indexParticipants[i];
            double val = v1.doubleValues[i];
            val += m_map.get(pos);
            m_map.put(pos, val);
        }
        m_vectors++;
    }

    public SparseVector getAverage(int rank) {
        SparseVector v = new SparseVector(m_map.size(), rank);
        TLongDoubleIterator iter = m_map.iterator();
        while (iter.hasNext()) {
            iter.advance();
            long pos = iter.key();
            double val = iter.value() / m_vectors;
            v.setNextElement(pos, val);
        }
        return v;
    }
}
