package ht.util.core.math;

/**
 * Copyright (c) 2003 - present HiTorro All rights reserved.
 * <p/>
 * User: chris
 * <p/>
 * Single threaded distance calculator for sparse vectors.  This implementation assumes extreemely sparse vectors and
 * does not attempt to optimize with any backout mechanism.
 */
public class SparseVectorEuclidDistance extends SparseVectorVisitor {
    private double m_result;

    public double calculateEuclidDistance(SparseVector v1, SparseVector v2) {
        return Math.sqrt(calculateEuclidDistanceSansSqrt(v1, v2));
    }

    public double calculateEuclidDistanceSansSqrt(final SparseVector v1, final SparseVector v2) {
        m_result = 0.0;
        this.visitAllElements(v1, v2);
        return m_result;
    }

    public void process(final double leftD, final double rightD) {
        double r = leftD - rightD;
        m_result += (r * r);
    }
}
