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

import java.util.Arrays;

/**
 * standard deviation.
 * <p/>
 * 1) compute mean value 2) subtract mean from each element. 3) square the results and sum together 4) divide by count
 * -1 5) root.
 */
public class SD {
    private int count = 0;
    private double array[] = new double[1];
    private double sum = 0.0;
    private int sizeOfData;

    public static void main(String args[]) {
        SD sd = new SD();

        sd.reset(new double[]{83, 70, 68, 64, 69, 75, 75, 72, 81});
        double mean = sd.getMean();
        double s = sd.getSd();
        double median = sd.getMedian();
    }

    public void reset(int sizeOfData) {
        count = 0;
        sum = 0.0;
        this.sizeOfData = sizeOfData;
        if (sizeOfData > array.length) {
            array = new double[sizeOfData];
        }
    }

    public void reset(double arrayIn[]) {
        sum = 0.0;
        this.sizeOfData = arrayIn.length;
        count = sizeOfData;
        array = arrayIn;
        for (int i = 0; i < array.length; i++) {
            sum += array[i];
        }
    }

    public void addData(double d) {
        sum += d;
        array[count++] = d;
    }

    public double getMean() {
        return sum / count;
    }

    public double getMedian() {
        Arrays.sort(array);
        int size = array.length;
        if (size % 2 == 0) {
            //even
            int index = size / 2;
            return (array[index] + array[index + 1]) / 2;
        }
        int index = ((size - 1) / 2) + 1;
        return array[index];
    }

    public double getSd() {
        double mean = getMean();
        double sdSum = 0.0;
        for (int i = 0; i < count; i++) {
            double delta = array[i] - mean;
            sdSum += (delta * delta);
        }
        double a = sdSum / (count - 1);
        return Math.sqrt(a);
    }
}
