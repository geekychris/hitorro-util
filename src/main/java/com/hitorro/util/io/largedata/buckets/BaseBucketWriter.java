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

import com.fasterxml.jackson.databind.JsonNode;
import com.hitorro.util.io.largedata.BaseFileAccessingObjectFactory;
import com.hitorro.util.io.largedata.buckets.containers.ArrayBucketWriterContainer;
import com.hitorro.util.io.largedata.buckets.containers.BucketWriterContainer;
import com.hitorro.util.io.largedata.buckets.containers.BucketWriterContainerBase;
import com.hitorro.util.io.largedata.buckets.containers.HashWrappedArrayBucketWriterContainer;

import java.io.IOException;
import java.util.List;

/**
 * <p/>
 * Bucket writer buffers the writing of a series of objects that need to be written in a specific order. For example. We
 * could be processing a series of documents for their urls we may want to writeout:
 * <p/>
 * docid6->url2 docid6->url1 docid3->url3 docid3->url2
 * <p/>
 * this should be written in the order:
 * <p/>
 * docid3->url3 docid3->url2 docid6->url1 docid6->url2
 * <p/>
 * Why write them out this way?  Well because you can take this output file with other output files ordered the same way
 * and you can apply them efficiently using an n way apply.
 */
public abstract class BaseBucketWriter<T> implements BucketWriter<T> {

    protected BucketWriterContainer m_container;
    protected BaseFileAccessingObjectFactory<T> fact;

    public BaseBucketWriter(int maxLength, BaseFileAccessingObjectFactory<T> fact) {
        if (fact.preferTreeBucket()) {
            m_container = new HashWrappedArrayBucketWriterContainer(maxLength, fact);
        } else {
            m_container = new ArrayBucketWriterContainer(maxLength, fact);
        }
        m_container.setBucketWriter(this);
    }

    public BaseBucketWriter(BucketWriterContainerBase container) {
        m_container = container;
        m_container.setBucketWriter(this);
    }

    public void setMaxBytesPerFlush(long bytes) {
        m_container.setMaxBytes(bytes);
    }

    public BucketWriterContainer<T> getContainer() {
        return m_container;
    }

    public void setContainer(BucketWriterContainer container) {
        m_container = container;
    }

    /**
     * Output the listFiles to the target (file, next step in the pipeline
     *
     * @param list
     * @return null or the listFiles.  The listFiles is returned if the callee no longer needs the listFiles, null is returned if the
     * callee is going to perform some asynchronous "thang" against the listFiles and therefor cannot return the
     * original listFiles back.
     */
    public abstract List<T> writeList(List<T> list) throws IOException;

    public abstract T[] writeArray(T list[], int maxIndex) throws IOException;

    public void close() throws IOException {
        flush();
    }

    public boolean add(T elem) throws IOException {
        return m_container.add(elem, 0);
    }

    public boolean add(T elem, long length) throws IOException {
        return m_container.add(elem, length);
    }

    public void flush() throws IOException {
        m_container.flush();
    }

    public int getSize() {
        return m_container.size();
    }


    //*************************** Sink **************************

    @Override
    public boolean init(JsonNode node) {
        return true;
    }

    @Override
    public boolean start() {
        return true;
    }

    @Override
    public boolean stop() throws IOException {
        close();
        return true;
    }
}
