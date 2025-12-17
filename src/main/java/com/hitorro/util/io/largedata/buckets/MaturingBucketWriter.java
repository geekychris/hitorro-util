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
package com.hitorro.util.io.largedata.buckets;

import com.hitorro.util.core.Constants;
import com.hitorro.util.core.thread.ThreadTimer;
import com.hitorro.util.io.largedata.buckets.containers.BucketWriterContainer;

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