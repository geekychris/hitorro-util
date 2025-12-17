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
package com.hitorro.util.datefilters;

import com.hitorro.util.core.UTCDateUtil;

import java.util.Date;

/**
 * Used to determine if a date provided is within a specified Range User: chris
 */
public class DateRangeFilter implements DateRangeFilterIntf {
    private int startDate[];
    private int endDate[];

    public DateRangeFilter(Date start, Date end) {
        init(start, end);
    }

    public DateRangeFilter(int[] start, int[] end) {
        startDate = start;
        endDate = end;
    }

    public int inRange(int[] parts, int depthIn) {
        int low = compare(parts, startDate, depthIn);
        if (low <= 0) {
            return low;
        }

        int high = compare(parts, endDate, depthIn);
        if (high > 0) {
            return 1;
        }
        return 0;
    }

    private int compare(int ymdparts[], int compareToMe[], int depthIn) {
        int depth = Math.min(depthIn, ymdparts.length);
        depth = Math.min(depth, compareToMe.length);
        for (int i = 0; i < depth; i++) {
            if (ymdparts[i] < compareToMe[i]) {
                return -1;
            }
            if (ymdparts[i] > compareToMe[i]) {
                return 1;
            }

        }
        return 0;
    }

    private void init(Date start, Date end) {
        startDate = UTCDateUtil.getYMDAsInArray(start);
        endDate = UTCDateUtil.getYMDAsInArray(end);
    }
}
