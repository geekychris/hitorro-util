package ht.util.io.largedata.buckets;

import ht.util.core.Constants;
import ht.util.core.thread.ThreadTimer;
import ht.util.io.largedata.buckets.containers.BucketWriterContainer;

import java.io.IOException;
import java.util.List;

/**
 * Wrapper around a real bucketwriter that allows us to flush the bucket every n seconds if it contains data.
 */
public abstract class MaturingBucketWriter<T> implements BucketWriter<T>, ContainerFlushNotification<T> {
    private int maxMaturitySecs;
    private ThreadTimer watchdogTimer;
    private long lastFlush = -1;
    private BucketWriter<T> realBucketWriter;

    private long bucketMaturityWindow = Constants.MillisInSecond * 60;

    public MaturingBucketWriter() {

    }

    public void setMaxBytesPerFlush(long bytes) {
        realBucketWriter.setMaxBytesPerFlush(bytes);
    }

    public synchronized boolean add(T elem) throws IOException {
        return realBucketWriter.add(elem);
    }

    public synchronized boolean add(T elem, long bytes) throws IOException {
        return realBucketWriter.add(elem, bytes);
    }

    public void flush() throws IOException {
        realBucketWriter.flush();
    }

    public List<T> writeList(List<T> list) throws IOException {
        return realBucketWriter.writeList(list);
    }

    public T[] writeArray(T list[], int maxIndex) throws IOException {
        return realBucketWriter.writeArray(list, maxIndex);
    }

    public void close() throws IOException {
        realBucketWriter.close();
    }

    public BucketWriterContainer<T> getContainer() {
        return realBucketWriter.getContainer();
    }

    public void setContainer(BucketWriterContainer<T> container) {
        realBucketWriter.setContainer(container);
    }

    protected void init(int maxMaturitySecs, BucketWriter<T> realBucketWriter) {
        this.realBucketWriter = realBucketWriter;
        this.maxMaturitySecs = maxMaturitySecs;
        watchdogTimer = new ThreadTimer(new Watchdog(this),
                Constants.MillisInSecond * this.maxMaturitySecs, true);
        watchdogTimer.start();
    }

    public synchronized void flushBucket() throws IOException {
        if (realBucketWriter.getSize() > 0) {
            flush();
        }
    }

    public int getSize() {
        return realBucketWriter.getSize();
    }

    /**
     * We flush our bucket if it contains stuff and we are over a certain amount of time not flushing.
     */
    public void flushBucketIfMature() throws IOException {
        long currT = System.currentTimeMillis();
        if (realBucketWriter.getSize() > 0) {
            if (currT - bucketMaturityWindow > lastFlush) {
                flushBucket();
            }
        }
    }

    /**
     * this method MUST be called follo
     *
     * @param o
     */
    public void flushNotify(BucketWriterContainer<T> o) {
        // the bucket writer flushed.
        lastFlush = System.currentTimeMillis();
    }
}