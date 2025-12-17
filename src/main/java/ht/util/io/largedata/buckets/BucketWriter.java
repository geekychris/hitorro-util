package ht.util.io.largedata.buckets;

import ht.util.core.iterator.sinks.Sink;
import ht.util.io.largedata.buckets.containers.BucketWriterContainer;

import java.io.IOException;
import java.util.List;

/**
 *
 */
public interface BucketWriter<T> extends Sink<T> {
    List<T> writeList(List<T> list) throws IOException;

    T[] writeArray(T list[], int maxIndex) throws IOException;

    void close() throws IOException;

    boolean add(T elem) throws IOException;

    boolean add(T elem, long length) throws IOException;

    void flush() throws IOException;

    int getSize();

    void setMaxBytesPerFlush(long bytes);

    BucketWriterContainer<T> getContainer();

    void setContainer(BucketWriterContainer<T> container);
}
