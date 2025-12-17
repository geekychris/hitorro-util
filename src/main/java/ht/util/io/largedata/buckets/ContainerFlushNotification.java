package ht.util.io.largedata.buckets;

import ht.util.io.largedata.buckets.containers.BucketWriterContainer;

/**
 * Copyright (c) 2003 - present HiTorro All rights reserved. User: chris
 */
public interface ContainerFlushNotification<T> {
    void flushNotify(BucketWriterContainer<T> o);
}
