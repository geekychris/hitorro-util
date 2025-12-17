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
