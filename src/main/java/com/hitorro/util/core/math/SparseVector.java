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

import cern.colt.matrix.impl.SparseDoubleMatrix1D;
import com.hitorro.util.core.string.Fmt;
import com.hitorro.util.core.tandemarrays.TandemArraySorterDoublePeer;
import com.hitorro.util.core.tandemarrays.TandemLongArraySorter;

/**

 * If I was smart enough I could figure out COLT for the vectors.  What wasnt obvious to me was the general efficiency
 * saying that our use of sparse vectors was to measure distance.  COLT implements them as hashtables.  I want to
 * iterate against the non zero elements in a more efficient way (yes a hashtable has constant lookup time, but its a
 * larger constant!).  Also to do things like normalize when I dont have access to the innerds is going to be a pain
 * while I dont know their api....so this is what I consider a hack to get going.
 * <p/>
 * euclidean norm:
 * <p/>
 * http://en.wikipedia.org/wiki/Norm_%28mathematics%29
 * <p/>
 * http://mathworld.wolfram.com/NormalizedVector.html
 * <p/>
 * <p/>
 * http://planetmath.org/encyclopedia/Normalize.html
 * <p/>
 * <p/>
 * Note from gary about cosine distance: Here's a little more. As you know, the vector "inner" or "dot" product is
 * calculated as the the sum of the element products. It's defined mathematically as x.y=|x||y|cos(A ) where A is the
 * angle between the vectors. So to calculate the angle between two vectors, you can use A = arccos(x.y/(|x||y|)). But
 * once you normalize the vectors, A=arccos(x.y), ie, the arccos of the dot product. So when you read that an IR system
 * uses "cosine angle" as a distance metric to measure document similarity, that's all they're doing
 */
public final class SparseVector<R> {
    int elementCount;
    int m_size;
    long indexParticipants[];
    double doubleValues[];
    double euclideanNorm;
    private R referrer;


    private int elemPositionCounter = 0;


    public SparseVector(final int elementCount, final int size) {
        m_size = size;
        this.elementCount = elementCount;
        indexParticipants = new long[elementCount];
        doubleValues = new double[elementCount];
    }

    public static void main(String args[]) {
        double f = MathUtil.probabilityDensity(6.2, 73, 66);
        // expect 0.0340...
        double d = MathUtil.entropy(2, 3, 4);
        d = MathUtil.entropy(2, 3);
        d = MathUtil.entropy(4, 0);
        d = MathUtil.entropy(3, 2);

        double a[][] = new double[3][];
        a[0] = new double[]{2, 3};
        a[1] = new double[]{4, 0};
        a[2] = new double[]{3, 2};

        d = MathUtil.averageInformation(a);

        SparseVector v = new SparseVector(3, 3);


        v.setNextElement(2, 4);
        v.setNextElement(1, 2);
        v.setNextElement(0, 1);

        v.sortByElemPosition();

        SparseVector v2 = new SparseVector(1, 3);


        v2.setNextElement(2, 2);
        v.calculateEuclidNormalize();
        v2.calculateEuclidNormalize();
        SparseVectorEuclidDistance dFunc = new SparseVectorEuclidDistance();
        double dist = dFunc.calculateEuclidDistance(v, v2);
        AverageSparseVector avg = new AverageSparseVector();
        avg.add(v);
        avg.add(v2);
        SparseVector v3 = avg.getAverage(3);
    }

    public R getReferrer() {
        return referrer;
    }

    public void setReferrer(R r) {
        referrer = r;
    }

    public String toString() {
        return Fmt.S("%s: s:%s, c:%s", referrer, m_size, elementCount);
    }

    public int getSize() {
        return m_size;
    }

    public int getElementCount() {
        return elementCount;
    }

    public long getElementPos(int i) {
        return indexParticipants[i];
    }

    public double getValue(int i) {
        return doubleValues[i];
    }

    /**
     * This method is slightly counter intuitive.  each call advances through the vector, elemPosition does not say
     * where we store it internally, it represents its position within the vector we are modelling.  If elemPositions
     * are provided out of sequence then the vector must be sorted.
     *
     * @param elemPosition
     * @param value
     */
    public final void setNextElement(final long elemPosition, final double value) {
        if (elemPositionCounter >= elementCount) {
            throw new ArrayIndexOutOfBoundsException();
        }
        indexParticipants[elemPositionCounter] = elemPosition;
        doubleValues[elemPositionCounter++] = value;
    }

    public final void sortByElemPosition() {
        TandemArraySorterDoublePeer peer = new TandemArraySorterDoublePeer(doubleValues);
        TandemLongArraySorter sorter = new TandemLongArraySorter();
        sorter.sort(indexParticipants, peer);
    }

    /**
     * Normalized vector or unit vector helps smooth out results and produces consistency.
     */
    public final void calculateEuclidNormalize() {
        double l = getEuclideanLength();

        for (int i = 0; i < elementCount; i++) {
            doubleValues[i] = doubleValues[i] / l;
        }
    }

    public final void computeNorm() {
        euclideanNorm = getEuclideanLength();
    }

    /**
     * Euclidean Length is the sqrt(sum of the squares of each element). This can be used to normalize the vector.
     *
     * @return length
     */
    public final double getEuclideanLength() {
        double d = 0.0;
        double t;
        for (int i = 0; i < elementCount; i++) {
            t = doubleValues[i];
            d += t * t;
        }
        return Math.sqrt(d);
    }

    /**
     * Just in case I have todo real math, here is a function to translateClassFilenameToCanonical the sparse vector to
     * a COLT vector (math library from CERN).
     * <p/>
     * Note that this casts long to int for the positional....so if we are using the long for say a 64 bit hash this
     * isnt going to work very well!
     *
     * @return COLT 1D matrix.
     */
    public final SparseDoubleMatrix1D getAsCOLTMatrix() {
        SparseDoubleMatrix1D m = new SparseDoubleMatrix1D(this.m_size);
        for (int i = 0; i < elementCount; i++) {
            m.setQuick((int) this.indexParticipants[i], doubleValues[i]);
        }
        return m;
    }

}
