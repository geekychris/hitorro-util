package ht.util.core.math;

import java.util.Arrays;

/**
 * Copyright (c) 2003 - present HiTorro All rights reserved. User: chris Date: Dec 5, 2005 Time: 4:39:38 PM Compute
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
