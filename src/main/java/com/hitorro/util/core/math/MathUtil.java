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


public class MathUtil {

    private static final double s_log2 = Math.log10(2);

    public static double[] calculateEuclidNormalize(double arr[], double l) {
        double arr2[] = new double[arr.length];
        for (int i = 0; i < arr.length; i++) {
            arr2[i] = arr[i] / l;
        }
        return arr2;
    }

    public static final double getEuclideanLength(double arr[]) {
        double d = 0.0;
        double t;
        for (int i = 0; i < arr.length; i++) {
            t = arr[i];
            d += t * t;
        }
        return Math.sqrt(d);
    }

    public static double[] tfidf(double arr[], double df[], double n) {
        double res[] = new double[arr.length];
        for (int i = 0; i < arr.length; i++) {
            res[i] = Math.log10(1.0 + arr[i]) * Math.log10(n / df[i]);
        }
        return res;
    }

    public static double dot(double arr[], double arr2[]) {
        double res = 0.0;
        for (int i = 0; i < arr.length; i++) {
            res += arr[i] * arr2[i];
        }
        return res;
    }

    /**
     * probability density function for normal distribution.
     *
     * @param theta - standard deviation
     * @param mu    - mean
     * @param x     - parameter to be
     */
    public static final double probabilityDensity(double theta, double mu, double x) {
        double delta = x - mu;
        double right = (delta * delta) / (2 * theta * theta);


        double fX = (1 / ((Math.sqrt(2 * Math.PI)) * theta)) * Math.exp(-right);
        return fX;
    }

    /**
     * Entropy of a given decision path with n classes.  For example: 2 yes, 3 no: entropy(2, 3)
     * <p/>
     * I believe this is sometimes refered to Information Gain.
     * <p/>
     * One can combine entropy calculations together to calculate average information by taking into account the number
     * of instances for all branches.
     *
     * @param args
     * @return
     */
    public static final double entropy(double... args) {
        double result = 0.0;
        double tot = 0;
        for (double a : args) {
            tot += a;
        }
        for (double a : args) {
            double p = a / tot;
            double r = (p * log2(p));
            result -= r;
        }
        return result;
    }

    /**
     * Average information value, takes 2d array, that is a 1d array for the class frequencies for each branch.
     *
     * @param branches
     * @return average information.
     */
    public static final double averageInformation(double branches[][]) {
        double totalInstances = 0;
        for (double branch[] : branches) {
            for (double b : branch) {
                totalInstances += b;
            }
        }
        double average = 0;
        for (double branch[] : branches) {
            double le = 0.0;
            for (double b : branch) {
                le += b;
            }
            double ratio = (le / totalInstances);
            double temp = (entropy(branch) * ratio);
            if (!Double.isNaN(temp)) {
                average += temp;
            }
        }
        return average;
    }

    /**
     * Calculating any base Log see http://www2.sims.berkeley.edu/courses/is255/f02/resources/Base2Logs.html
     *
     * @param number
     * @param base
     * @return LOGbase(number)
     */
    public static final double log(double number, double base) {
        return Math.log10(number) / Math.log10(base);
    }

    /**
     * Log2(number) see: http://www2.sims.berkeley.edu/courses/is255/f02/resources/Base2Logs.html
     *
     * @param number
     * @return
     */
    public static final double log2(double number) {
        return Math.log10(number) / s_log2;
    }
}
