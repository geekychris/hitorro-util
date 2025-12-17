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
package com.hitorro.util.core.date;

import com.hitorro.util.core.tandemarrays.TandemArraySorterLongPeer;
import com.hitorro.util.core.tandemarrays.TandemLongArraySorter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Given a time in millis, provide the start time in millis of a date range snapped to the appropriate date resolution
 * So for instance, if one provides a date resolution of 1 wk, the times returned snap to start of that period.
 * <p/>
 * This attempts to be a fast way to get this for segmenting objects by their create/ fetch / whatever date.
 * <p/>
 * Note that the more entries that are in this structure the slower the retrieval becomes.  This algorithm does not take
 * account of any temporal effect from the requestor, if the times for instance are mostly reflective of current time,
 * it maybe advantageous to recreate this structure periodically.  The root cause is that we simply do a binary search
 * of an ordered set of time ranges to look for a near test.
 */
public final class DateRangeSnapper {
    private long start[] = new long[0];
    private long end[] = new long[0];
    private List<DateRange> list = new ArrayList();
    private DateResolution res;

    private TandemLongArraySorter sorter;
    private TandemArraySorterLongPeer peer;

    private int newUps = 0;

    public DateRangeSnapper(DateResolution res) {
        this.res = res;
        long now = new Date().getTime();
        sorter = new TandemLongArraySorter();
        peer = new TandemArraySorterLongPeer();

    }

    public int getNewCount() {
        return newUps;
    }

    public final long getSnappedTime(long time) {
        int index = Arrays.binarySearch(start, 0, start.length, time);
        if (index != -1) {

            if (index < 0) {
                index = -index - 2;
            }
            if (end[index] >= time && start[index] <= time) {
                return start[index];
            }
        }
        return addRange(time);
    }

    private final long addRange(long time) {
        newUps++;
        DateRange dr = new DateRange(time, DateResolution.Second, res);
        long returnMe = dr.getStart();
        list.add(dr);
        int size = list.size();
        start = new long[size];
        end = new long[size];
        for (int i = 0; i < size; i++) {
            dr = list.get(i);
            start[i] = dr.getStart();

            end[i] = dr.getEnd();
        }

        peer.set(end);
        sorter.sort(start, peer);
        return returnMe;
    }
}

