package ht.util.core.math;

import ht.util.core.Console;

/**
 * .
 */
public class SparseVectorCosineDistance extends SparseVectorVisitor {
    private double m_result;

    public double calculateEuclidDistanceSansSqrt(final SparseVector v1, final SparseVector v2) {
        m_result = 0.0;
        this.visitOnlyIntersection(v1, v2);
        return m_result;
    }

    public void process(final double leftD, final double rightD) {
        if (leftD == 0.0 || rightD == 0.0) {
            Console.println();
        }
        m_result += leftD * rightD;
    }
}
