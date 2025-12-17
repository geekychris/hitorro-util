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
package com.hitorro.util.io.largedata.buckets.containers;

import com.hitorro.util.core.iterator.AbstractIterator;
import com.hitorro.util.core.iterator.ArrayIterator;
import com.hitorro.util.core.iterator.LikeRowMerger;
import com.hitorro.util.io.largedata.BaseFileAccessingObjectFactory;
import com.hitorro.util.io.largedata.buckets.BucketWriter;
import com.hitorro.util.io.largedata.buckets.ContainerFlushNotification;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 *
 */
public class ArrayBucketWriterContainer<T> implements BucketWriterContainer<T> {
    protected LikeRowMerger<T> m_merger;
    protected Comparator<T> m_comparator;
    protected int flushCount;
    protected BucketWriter<T> m_writer;
    private int m_maxLength;
    private T arr[];
    private int index = 0;
    private BaseFileAccessingObjectFactory<T> factory;
    private long maxBytes = -1;
    private long currentBytes = 0;
    private List<ContainerFlushNotification> notifications = new ArrayList();

    public ArrayBucketWriterContainer(int maxLength, BaseFileAccessingObjectFactory<T> factory) {
        m_maxLength = maxLength;
        m_merger = factory.getRowMerger();
        m_comparator = factory.getDefaultComparitor();
        this.factory = factory;
        arr = getArray(maxLength);
    }

    public void setMaxBytes(long bytes) {
        maxBytes = bytes;
    }

    @Override
    public int writeCount() {
        return flushCount;
    }

    @Override
    public void reset() {
        index = 0;
    }

    @Override
    public AbstractIterator<T> getFlushAsIterator() {
        sortIt();
        return new ArrayIterator(arr, index - 1);
    }


    public void setBucketWriter(BucketWriter<T> writer) {
        m_writer = writer;
    }

    public void addNotificationHandler(ContainerFlushNotification handler) {
        notifications.add(handler);
    }

    public void notifyHandlers() {
        for (ContainerFlushNotification handler : notifications) {
            handler.flushNotify(this);
        }
    }


    public T[] getArray(int size) {
        return factory.getArray(size);
    }

    public boolean add(T elem, long bytes) throws IOException {
        arr[index++] = elem;
        currentBytes += bytes;
        if (m_maxLength != -1 && index >= m_maxLength || maxBytes > -1 && this.currentBytes > this.maxBytes) {
            currentBytes = 0;
            flush();
        }
        return true;
    }

    public int size() {
        return index;
    }

    public void flush() throws IOException {
        sortIt();
        flushCount++;
        T tee[] = m_writer.writeArray(arr, index - 1);
        if (tee == null) {
            arr = getArray(m_maxLength);
        }
        index = 0;
        // notify anyone of the flush
        notifyHandlers();
    }

    private void sortIt() {
        Arrays.sort(arr, 0, index, m_comparator);
        if (m_merger != null) {
            index = removeDuplicates();
        }
    }

    /**
     * Remove duplicate items from a listFiles.  First it sorts the listFiles by the comparitor.
     *
     * @return
     */
    private final int removeDuplicates() {
        int insertInd = 0;
        int dedupe = 0;
        for (int i = 1; i < index; i++) {
            T older = arr[insertInd];
            if (m_comparator.compare(arr[i], older) == 0) {
                arr[insertInd] = m_merger.apply(older, arr[i]);
                dedupe++;
            } else {
                arr[++insertInd] = arr[i];
            }
        }

        int newIndex = insertInd + 1;
        index = newIndex;
        return index;
    }
}
