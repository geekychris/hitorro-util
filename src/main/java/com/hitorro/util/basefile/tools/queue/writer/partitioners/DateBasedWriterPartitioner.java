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
package com.hitorro.util.basefile.tools.queue.writer.partitioners;

import com.hitorro.util.core.Constants;
import com.hitorro.util.core.UTCDateUtil;

import java.util.Date;


/**
 * A partitioner that assumes each item coming into the queue has an incrementing date that can be used for partitioning
 * on a day basis.
 * <p/>
 * Overiding the setPartitionBoundaries method can be used to vary that resolution.
 */
public abstract class DateBasedWriterPartitioner<T> implements WriterPartitioner<T> {
    protected T current;
    private Date currDate;
    private long startDate = -1;
    private long endDate = -1;

    public abstract Date getDateField();

    public T getCurrent() {
        return current;
    }

    public void setCurrent(T o) {
        current = o;
        Date qDate = getDateField();

        if (qDate == null) {
            // Not sure what the old queue did, but surely it didnt do this for feature items.
            qDate = new Date();
        }
        currDate = qDate;
    }

    public boolean hasCurrent() {
        return current != null;
    }

    public long getPartitionSequenceNumber() {
        return currDate.getTime();
    }

    public boolean isWithinFileRange() {
        long t = currDate.getTime();
        return startDate <= t && t < endDate;

    }

    public void reset() {
        startDate = -1;
        endDate = -1;

    }

    public void setPartitionBoundaries() {
        startDate = UTCDateUtil.getStartOfDay(currDate).getTime();
        endDate = startDate + (Constants.MillisInHour * 24);
    }
}
