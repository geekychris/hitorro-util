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

import com.hitorro.util.io.largedata.buckets.BucketWriter;
import com.hitorro.util.io.largedata.buckets.ContainerFlushNotification;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


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
