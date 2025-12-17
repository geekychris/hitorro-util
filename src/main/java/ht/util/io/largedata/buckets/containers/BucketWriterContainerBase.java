package ht.util.io.largedata.buckets.containers;

import ht.util.io.largedata.buckets.BucketWriter;
import ht.util.io.largedata.buckets.ContainerFlushNotification;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Copyright (c) 2003 - present HiTorro All rights reserved.
 * <p/>
 * User: chris
 */
public abstract class BucketWriterContainerBase<T> implements BucketWriterContainer<T> {
    protected BucketWriter<T> m_writer;
    protected List<T> m_list = new ArrayList<T>();
    protected int flushCount;
    private List<ContainerFlushNotification> notifications = new ArrayList();

    public void setBucketWriter(BucketWriter<T> writer) {
        m_writer = writer;
    }

    public abstract boolean add(T elem) throws IOException;

    public abstract void flush() throws IOException;


    public void addNotificationHandler(ContainerFlushNotification handler) {
        notifications.add(handler);
    }

    public void notifyHandlers() {
        for (ContainerFlushNotification handler : notifications) {
            handler.flushNotify(this);
        }
    }

    public int size() {
        return m_list.size();
    }

    @Override
    public int writeCount() {
        return flushCount;
    }
}
