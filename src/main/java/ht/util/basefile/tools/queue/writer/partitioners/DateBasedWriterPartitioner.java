package ht.util.basefile.tools.queue.writer.partitioners;

import ht.util.core.Constants;
import ht.util.core.UTCDateUtil;

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
