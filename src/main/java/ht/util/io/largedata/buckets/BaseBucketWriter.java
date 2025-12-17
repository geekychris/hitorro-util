package ht.util.io.largedata.buckets;

import com.fasterxml.jackson.databind.JsonNode;
import ht.util.io.largedata.BaseFileAccessingObjectFactory;
import ht.util.io.largedata.buckets.containers.ArrayBucketWriterContainer;
import ht.util.io.largedata.buckets.containers.BucketWriterContainer;
import ht.util.io.largedata.buckets.containers.BucketWriterContainerBase;
import ht.util.io.largedata.buckets.containers.HashWrappedArrayBucketWriterContainer;

import java.io.IOException;
import java.util.List;

/**
 * Copyright (c) 2003 - present HiTorro All rights reserved. User: chris Date: Jan 31, 2005 Time: 2:00:48 PM
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
