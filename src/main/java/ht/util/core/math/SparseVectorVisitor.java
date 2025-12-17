package ht.util.core.math;

/**
 * Copyright (c) 2003 - present HiTorro All rights reserved.
 * <p/>
 * User: chris
 */
public abstract class SparseVectorVisitor {
    private long m_leftI;
    private long m_rightI;
    private int m_left;
    private int m_right;
    private SparseVector m_v1;
    private SparseVector m_v2;

    protected final void visitAllElements(SparseVector v1In, SparseVector v2In) {
        m_left = 0;
        m_right = 0;
        m_v1 = v1In;
        m_v2 = v2In;

        m_leftI = m_v1.m_indexParticipants[m_left];
        m_rightI = m_v2.m_indexParticipants[m_right];
        while (m_left < m_v1.m_elementCount && m_right < m_v2.m_elementCount) {
            if (m_leftI < m_rightI) {
                process(m_v1.m_doubleValues[m_left], 0.0);
                advanceLeft();
            } else if (m_leftI > m_rightI) {
                process(0.0, m_v2.m_doubleValues[m_right]);
                advanceRight();
            } else {
                process(m_v1.m_doubleValues[m_left], m_v2.m_doubleValues[m_right]);
                advanceRight();
                advanceLeft();
            }
        }
        // deal with tail end
        while (m_left < m_v1.m_elementCount) {
            process(m_v1.m_doubleValues[m_left], 0.0);
            advanceLeft();
        }
        while (m_right < m_v2.m_elementCount) {
            process(0.0, m_v2.m_doubleValues[m_right]);
            advanceRight();
        }
    }

    /**
     * only visit those elements that are in common to both vectors.
     *
     * @param v1In
     * @param v2In
     */
    protected final void visitOnlyIntersection(SparseVector v1In, SparseVector v2In) {
        m_left = 0;
        m_right = 0;
        m_v1 = v1In;
        m_v2 = v2In;

        m_leftI = m_v1.m_indexParticipants[m_left];
        m_rightI = m_v2.m_indexParticipants[m_right];
        while (m_left < m_v1.m_elementCount && m_right < m_v2.m_elementCount) {
            if (m_leftI < m_rightI) {
                advanceLeft();
            } else if (m_leftI > m_rightI) {
                advanceRight();
            } else {
                process(m_v1.m_doubleValues[m_left], m_v2.m_doubleValues[m_right]);
                advanceRight();
                advanceLeft();
            }
        }
    }

    private final void advanceLeft() {
        m_left++;
        if (m_left < m_v1.m_elementCount) {
            m_leftI = m_v1.m_indexParticipants[m_left];
        }
    }

    private final void advanceRight() {
        m_right++;
        if (m_right < m_v2.m_elementCount) {
            m_rightI = m_v2.m_indexParticipants[m_right];
        }
    }

    public abstract void process(double leftD, double rightD);
}
