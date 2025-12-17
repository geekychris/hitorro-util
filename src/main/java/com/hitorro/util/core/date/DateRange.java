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

import com.hitorro.util.core.UTCDateUtil;
import com.hitorro.util.core.string.Fmt;
import com.hitorro.util.core.string.StringUtil;

import java.text.ParseException;
import java.util.Date;

/**
 *
 */
public class DateRange {

    private DateResolution res;
    private DateResolution range;
    private Date start;
    private Date end;
    private long duration;
    private long startMillis;
    private long endMillis;


    /**
     * Get a DR based upon some time in millis that does not have to align to week or day, failing that it does not
     * allign at all
     *
     * @param startMillis
     * @param res
     * @param range
     */
    public DateRange(long startMillis, DateResolution res, DateResolution range) {
        start = new Date(startMillis);
        if (range.getCanonicalResolution() == DateResolution.Week) {
            start = UTCDateUtil.snapToWeek(start);
        } else if (range.getCanonicalResolution() == DateResolution.Week) {
            start = UTCDateUtil.snapToDay(start);
        }

        end = new Date(start.getTime() + range.getDurationInMillis());
        setupMillis();
        this.res = res;
        range = DateResolution.getDateResolutionFromMillisRange(duration);
    }

    public DateRange(Date start, Date end, DateResolution res) {
        this.start = start;
        this.end = end;
        this.res = res;
        setupMillis();
        range = DateResolution.getDateResolutionFromMillisRange(duration);
    }

    public DateRange(String start, String end) throws ParseException {
        init(start, end);
    }

    public DateRange(String dateString) throws ParseException {
        String parts[] = StringUtil.tokenizeFromSingleChar(dateString, "-");
        if (parts.length >= 2) {
            init(parts[0], parts[1]);
        }
    }

    public int hashCode() {
        return start.hashCode() ^ end.hashCode();
    }

    public boolean equals(Object o) {
        if (o instanceof DateRange) {
            DateRange other = (DateRange) o;
            return start.equals(other.start) && end.equals(other.end);
        }
        return false;
    }

    /**
     * provides a date range that is the next in sequence.  Usefull for advancing through days or weeks.
     *
     * @return
     */
    public DateRange getNextInSeries() {
        long newStart = end.getTime() + 1;
        return new DateRange(new Date(newStart), new Date(newStart + duration), res);
    }

    private void init(String startS, String endS) throws ParseException {
        if (startS.length() == 6) {
            res = DateResolution.Day;
        } else {
            res = DateResolution.Hour;
        }
        start = res.parse(startS);
        end = res.parse(endS);

        setupMillis();
        range = DateResolution.getDateResolutionFromMillisRange(duration);
    }

    private void setupMillis() {
        duration = end.getTime() - start.getTime();
        startMillis = start.getTime();
        endMillis = end.getTime();
    }

    public DateResolution getDateRange() {
        return range;
    }

    public long getDuration() {
        return duration;
    }


    public String getDateRangeString() {
        return Fmt.S("%s-%s", res.getFormatted(start), res.getFormatted(end));
    }

    public DateResolution getResolution() {
        return res;
    }

    public long getStart() {
        return startMillis;
    }

    public long getEnd() {
        return endMillis;
    }


    public Date getStartDate() {
        return start;
    }

    public Date getEndDate() {
        return end;
    }


}
