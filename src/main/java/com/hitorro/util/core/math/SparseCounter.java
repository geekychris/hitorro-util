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

import gnu.trove.iterator.TIntIntIterator;
import gnu.trove.map.hash.TIntIntHashMap;
import com.hitorro.util.basefile.fs.BaseFile;
import com.hitorro.util.basefile.tools.BaseFileUtil;
import com.hitorro.util.core.Console;
import com.hitorro.util.core.string.Fmt;
import com.hitorro.util.core.tandemarrays.TandemArraySorterIntPeer;
import com.hitorro.util.core.tandemarrays.TandemIntArraySorter;
import com.hitorro.util.io.csv.CSVFileWriter;
import com.hitorro.util.io.csv.CSVReader;
import com.hitorro.util.io.csv.ColumnTableMeta;
import com.hitorro.util.io.csv.csvconsumer.CSVConsumer;

import java.io.IOException;
import java.io.PrintStream;

/**
 * Sparse integer counters.  Providing ways to write and read to csv file format
 */
public class SparseCounter {
    private TIntIntHashMap map = new TIntIntHashMap();
    private String name;
    private String key;
    private String value;
    private int numberOfValues;

    private int[][] sparseCount = null;

    public SparseCounter(String name, String key, String val) {
        this.name = name;
        this.key = key;
        this.value = val;
    }

    public String getStringRep() {
        StringBuilder sb = new StringBuilder();
        int arr[][] = getCountsSparse();
        Console.bprint(sb, "%s/%s/%s:", name, key, value);
        for (int i = 0; i < arr[0].length; i++) {
            Console.bprint(sb, "[%s,%s]", arr[0][i], arr[1][i]);
        }
        return sb.toString();
    }

    /**
     * Group x axis by bucket size
     *
     * @param bucketSize
     * @return
     */
    public SparseCounter getBucket(int bucketSize) {
        SparseCounter sc = new SparseCounter(name, key, value);
        int counts[][] = getCountsSparse();
        int currBucket = 0;
        int sum = 0;
        for (int i = 0; i < counts[0].length; i++) {
            int thisBucket = counts[0][i] / bucketSize;
            if (currBucket != thisBucket) {
                //
                if (sum > 0) {
                    int v = sum / bucketSize;
                    if (v > 0) {
                        sc.set(currBucket * bucketSize, sum / bucketSize);
                    }
                }
                currBucket = thisBucket;
                sum = 0;
            }
            sum += counts[1][i];
        }
        if (sum > 0) {
            int v = sum / bucketSize;
            if (v > 0) {
                sc.set(currBucket * bucketSize, v);
            }
        }
        return sc;
    }

    /**
     * value - avg / sd
     *
     * @param value
     * @return
     */
    public double getStandardUnit(int value) {
        return value - getMean() / getSD();
    }

    /**
     * value = avg + (su * sd)
     *
     * @param standardUnit
     * @return
     */
    public double getValueFromStandardUnit(double standardUnit) {
        return getMean() + (standardUnit * getSD());
    }


    /**
     * Get the root mean square of the values.
     *
     * @return RMS
     */
    public double getRMS() {
        double sum = 0;
        TIntIntIterator iter = map.iterator();
        int i = 0;
        while (iter.hasNext()) {
            iter.advance();
            double s = iter.key();
            int freq = iter.value();
            s = s * s;
            sum += (s * freq);
        }
        return sum / numberOfValues;
    }


    public double getMedian() {
        return 0;
    }

    public int getLowerRange() {
        int[][] arr = getCountsSparse();
        return 0;
    }

    public int getUpperRange() {
        return 0;
    }


    public void increment(int index, int amount) {
        if (map.contains(index)) {
            map.adjustValue(index, map.get(index) + amount);
        } else {
            map.put(index, amount);
        }
    }

    void set(int index, int value) {
        sparseCount = null;
        map.put(index, value);
    }

    public void increment(int index) {
        sparseCount = null;
        if (map.contains(index)) {
            map.increment(index);
        } else {
            map.put(index, 1);
        }
    }

    public void readFromCSV(BaseFile bf) throws IOException {
        Consumer c = new Consumer(this, key, value);
        CSVReader reader = new CSVReader(bf, "utf-8");
        reader.readLines(c);
    }

    public void writeSparseToCSV(BaseFile bf) {
        PrintStream ps = BaseFileUtil.bf2utf8printstream.apply(bf);
        CSVFileWriter writer = new CSVFileWriter(ps, new String[]{key, value});
        int arr[][] = getCountsSparse();
        for (int i = 0; i < arr[0].length; i++) {
            writer.writeRow(new Object[]{arr[0][i], arr[1][i]});
        }
        writer.close();
    }

    public void writeDenseToCSV(BaseFile bf) {
        PrintStream ps = BaseFileUtil.bf2utf8printstream.apply(bf);
        CSVFileWriter writer = new CSVFileWriter(ps, new String[]{key, value});
        int arr[] = getCountsDense();
        for (int i = 0; i < arr.length; i++) {
            writer.writeRow(new Object[]{i, arr[i]});
        }
        writer.close();
    }

    public int[][] getCountsSparse() {
        if (sparseCount != null) {
            return sparseCount;
        }

        int arr[][] = new int[2][];
        arr[0] = new int[map.size()];
        arr[1] = new int[map.size()];
        TIntIntIterator iter = map.iterator();
        int i = 0;
        while (iter.hasNext()) {
            iter.advance();
            arr[0][i] = iter.key();
            arr[1][i++] = iter.value();
        }
        TandemIntArraySorter sorter = new TandemIntArraySorter();
        TandemArraySorterIntPeer peer = new TandemArraySorterIntPeer();
        peer.set(arr[1]);
        sorter.sort(arr[0], peer);
        sparseCount = arr;
        return arr;
    }

    public int[] getCountsDense() {
        int aIn[][] = getCountsSparse();
        int l = aIn[0][aIn[0].length - 1];
        // at the last position is the index of the last position, of which means the length of the array is +1
        int arr[] = new int[l + 1];

        for (int i = 0; i < aIn[0].length; i++) {
            arr[aIn[0][i]] = aIn[1][i];
        }
        return arr;
    }


    public double getMean() {
        double d = 0;
        numberOfValues = 0;
        TIntIntIterator iter = map.iterator();
        int i = 0;
        while (iter.hasNext()) {
            iter.advance();
            int ord = iter.key();
            int freq = iter.value();
            d += (ord * freq);
            numberOfValues += freq;
        }
        return d / numberOfValues;
    }

    public double getVariance() {
        double sum = 0;
        double mean = getMean();
        TIntIntIterator iter = map.iterator();
        int i = 0;
        while (iter.hasNext()) {
            iter.advance();
            double ord = iter.key();
            int freq = iter.value();
            double s = ord - mean;
            s = s * s;
            sum += (s * freq);
        }
        return sum / numberOfValues;
    }

    public double getSD() {
        double var = getVariance();
        return Math.sqrt(var);
    }

    public void write(BaseFile parentDir, String rootName, boolean sparse) {
        String n = Fmt.S("%s-%s.csv", rootName, name);
        BaseFile file = parentDir.getChild(n);
        if (sparse) {
            writeSparseToCSV(file);
        } else {
            writeDenseToCSV(file);
        }
        Console.println("name %s, mean %s, SD: %s", n, this.getMean(), this.getSD());
    }
}

class Consumer implements CSVConsumer {
    private SparseCounter sc;
    private ColumnTableMeta ctm;
    private String key;
    private int keyPos;
    private int valuePos;
    private String value;

    public Consumer(SparseCounter sc, String key, String value) {
        this.sc = sc;
        this.key = key;
        this.value = value;
    }

    @Override
    public void line(final int rowCount, final String[] line) {
        if (rowCount == 0) {
            ctm = ColumnTableMeta.init(line);
            keyPos = ctm.getColumnInt(key);
            valuePos = ctm.getColumnInt(value);
        } else {
            sc.set(Integer.parseInt(line[keyPos]), Integer.parseInt(line[valuePos]));
        }
    }
}