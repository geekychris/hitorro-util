package ht.util.io.largedata.buckets.containers;

import ht.util.core.iterator.AbstractIterator;
import ht.util.io.largedata.buckets.BucketWriter;
import ht.util.io.largedata.buckets.ContainerFlushNotification;

import java.io.IOException;

/**
 *
 */
public interface BucketWriterContainer<T> {
    void setBucketWriter(BucketWriter<T> writer);

    boolean add(T elem, long bytes) throws IOException;

    void flush() throws IOException;

    void addNotificationHandler(ContainerFlushNotification handler);

    void notifyHandlers();

    int size();

    void setMaxBytes(long bytes);

    int writeCount();

    void reset();

    /**
     * Only if the flush did not get triggered can we consume the sorted elements as an iterator.
     *
     * @return
     */
    AbstractIterator<T> getFlushAsIterator();
}
