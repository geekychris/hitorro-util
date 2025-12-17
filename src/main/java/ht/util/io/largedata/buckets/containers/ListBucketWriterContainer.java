package ht.util.io.largedata.buckets.containers;

import ht.util.core.iterator.AbstractIterator;
import ht.util.core.iterator.CollectionIterator;
import ht.util.core.iterator.LikeRowMerger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Copyright (c) 2003 - present HiTorro All rights reserved.
 * <p/>
 * User: chris
 */
public class ListBucketWriterContainer<T> extends BucketWriterContainerBase<T> {
    private int m_maxLength;
    private LikeRowMerger<T> m_merger;

    private Comparator<T> m_comparator;

    private long maxBytes = -1;
    private long currentBytes = 0;

    public ListBucketWriterContainer(int maxLength, LikeRowMerger<T> merger, Comparator<T> comp) {
        m_maxLength = maxLength;
        m_merger = merger;
        m_comparator = comp;
    }

    public boolean add(T elem) throws IOException {
        return add(elem, 0);
    }

    @Override
    public boolean add(final T elem, final long bytes) throws IOException {
        if (elem == null) {
            return false;
        }
        m_list.add(elem);
        currentBytes += bytes;
        if (m_maxLength != -1 && m_list.size() > m_maxLength || maxBytes > -1 && currentBytes > maxBytes) {
            currentBytes = 0;
            flush();
        }
        return true;
    }

    public void flush() throws IOException {
        sortIt();
        List<T> t = m_writer.writeList(m_list);
        if (t == null) {
            m_list = new ArrayList<T>();
        } else {
            m_list.clear();
        }
        // notify anyone of the flush
        notifyHandlers();
    }

    private void sortIt() {
        if (m_merger != null) {
            removeDuplicates();
        } else {
            if (m_comparator != null) {
                Collections.sort(m_list, m_comparator);
            }
        }
    }

    @Override
    public void setMaxBytes(final long bytes) {
        maxBytes = bytes;
    }

    @Override
    public void reset() {
        m_list.clear();
    }

    @Override
    public AbstractIterator<T> getFlushAsIterator() {
        sortIt();
        return new CollectionIterator<T>(m_list);
    }

    /**
     * Remove duplicate items from a listFiles.  First it sorts the listFiles by the comparator.
     *
     * @return
     */
    private final boolean removeDuplicates() {
        Collections.sort(m_list, m_comparator);

        int size = m_list.size();
        for (int i = size - 1; i > 0; i--) {
            if (m_comparator.compare(m_list.get(i), m_list.get(i - 1)) == 0) {
                // remove the lower one.
                T older = m_list.get(i - 1);
                T newer = m_list.remove(i);
                T elem = m_merger.apply(older, newer);
                m_list.set(i - 1, elem);
            }
        }
        return true;
    }

}
