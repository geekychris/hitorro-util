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
package com.hitorro.util.io.csv.query;

import com.hitorro.util.basefile.fs.BaseFile;
import com.hitorro.util.core.iterator.CloseableIterator;
import com.hitorro.util.core.math.SparseCounter;
import com.hitorro.util.core.opers.HTPredicate;

import java.io.IOException;

/**
 * Set of methods associated with computing statistics on a table
 */
public class StatisticsTable {
    private BaseFile inputFile;
    private BaseFile cacheDir;
    private CSVTableMeta csvMeta;

    public StatisticsTable(BaseFile inputFile, CSVTableMeta csvMeta, BaseFile cacheDir) {
        this.inputFile = inputFile;
        this.cacheDir = cacheDir;
        this.csvMeta = csvMeta;
    }

    /**
     * @param fieldY
     * @param fieldX
     * @param counterYs
     * @param counterXs
     * @param filter
     * @return
     * @throws IOException
     */
    public double computeR(String fieldY, String fieldX, String counterYs, String counterXs, HTPredicate<String[]> filter) throws IOException {
        return computeR(fieldY, fieldX, cacheDir.getChild("%s.csv", counterYs), cacheDir.getChild("%s.csv", counterXs), filter);
    }


    /**
     * Compute the correlation coefficient (R)
     *
     * @param fieldY
     * @param fieldX
     * @param counterYs
     * @param counterXs
     * @param filter
     * @return
     * @throws IOException
     */
    public double computeR(String fieldY, String fieldX, BaseFile counterYs, BaseFile counterXs, HTPredicate<String[]> filter) throws IOException {
        SparseCounter counterY = new SparseCounter(counterYs.getNameSansExtension(), "ord", "freq");
        counterY.readFromCSV(counterYs);
        SparseCounter counterX = new SparseCounter(counterXs.getNameSansExtension(), "ord", "freq");
        counterX.readFromCSV(counterXs);
        return computeR(fieldY, fieldX, counterY, counterX, filter);
    }

    /**
     * Compute correlation coefficient between two
     *
     * @param fieldY
     * @param fieldX
     * @param counterY
     * @param counterX
     * @return
     */
    public double computeR(String fieldY, String fieldX, SparseCounter counterY, SparseCounter counterX, HTPredicate<String[]> filter) throws IOException {
        double meanX = counterX.getMean();
        double meanY = counterY.getMean();
        double sdX = counterX.getSD();
        double sdY = counterY.getSD();
        CSVQuery q = CSVQuery.selectFromMeta(this.inputFile, this.csvMeta, fieldY, fieldX);
        q.where(filter);
        CloseableIterator<Object[]> iter = q.execute();
        int i = 0;
        double accum = 0;
        while (iter.hasNext()) {
            Object line[] = iter.next();
            double y = ((Integer) line[0]).intValue();
            double x = ((Integer) line[1]).intValue();
            i++;
            accum += ((y - meanY) / sdY) * ((x - meanX) / sdX);
        }
        double iD = i;
        return (1 / (iD - 1)) * accum;
    }
}

