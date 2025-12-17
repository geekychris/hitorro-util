package ht.util.core.math;


import gnu.trove.iterator.TLongDoubleIterator;
import gnu.trove.map.hash.TLongDoubleHashMap;

/**
 * Copyright (c) 2003 - present HiTorro All rights reserved.
 * <p/>
 * User: chris Given a series of sparse vectors, generate a sparseVector which is the average of all (that is any
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
        for (int i = 0; i < v1.m_elementCount; i++) {
            long pos = v1.m_indexParticipants[i];
            double val = v1.m_doubleValues[i];
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
