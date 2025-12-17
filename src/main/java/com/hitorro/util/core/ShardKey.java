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
package com.hitorro.util.core;

import com.hitorro.util.core.date.DateRange;
import com.hitorro.util.core.string.Fmt;
import com.hitorro.util.core.string.StringUtil;

import java.text.ParseException;

/**
 *
 */
public class ShardKey {
    private DateRange dateRange;
    private String fullName;
    private String subSegment;

    public ShardKey(String fullName) throws ParseException {
        this.fullName = fullName;
        String parts[] = StringUtil.tokenizeFromSingleChar(fullName, "-");
        if (parts.length >= 2) {
            dateRange = new DateRange(parts[0], parts[1]);
            if (parts.length > 2) {
                subSegment = parts[2];
            }
        } else {
            throw new ParseException("not enough parts to name", parts.length);
        }
        finalizeShardKey();
    }

    public String toString() {
        return fullName;
    }

    public String getFullName() {
        return fullName;
    }

    private void finalizeShardKey() {
        fullName = Fmt.S("%s-%s", dateRange.getDateRangeString(), subSegment);
    }

    public String getSubSegment() {
        return subSegment;
    }

    public DateRange getDateRange() {
        return dateRange;
    }
}
