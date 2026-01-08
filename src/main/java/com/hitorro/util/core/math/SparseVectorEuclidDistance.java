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

/**

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
