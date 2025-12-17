package ht.util.io.largedata.buckets.containers;

import ht.util.core.iterator.AbstractIterator;
import ht.util.core.iterator.CollectionIterator;
import ht.util.core.iterator.LikeRowMerger;

import java.io.IOException;
import java.util.*;

/**
 * Copyright (c) 2003 - present HiTorro All rights reserved.
 * <p/>
 * User: chris
 * <p/>
 * Manage a bucket of things by their key.  Merges them together and when there is enough of them spews them out
 */
public class TreeMapBucketWriterContainer<T> extends BucketWriterContainerBase<T> {
    private int m_maxLength;
    private TreeMap<T, T> m_map = new TreeMap<T, T>();
    private Comparator<T> m_comp;
    private LikeRowMerger<T> m_merger;


    public TreeMapBucketWriterContainer(int maxLength, Comparator<T> comp, LikeRowMerger<T> merger) {
        m_maxLength = maxLength;
        m_comp = comp;
        m_merger = merger;
    }

    public TreeMapBucketWriterContainer(int maxLength, LikeRowMerger<T> merger) {
        m_maxLength = maxLength;
        m_comp = null;
        m_merger = merger;
    }

    public boolean add(T newer) throws IOException {
        T old = m_map.get(newer);
        if (old == null) {
            m_map.put(newer, newer);
        } else {
            newer = m_merger.apply(old, newer);
            if (newer != old) {
                // try to avoid doing a put if we modified the one in the apply already.
                m_map.put(newer, newer);
            }
        }

        if (m_map.size() > m_maxLength) {
            flush();
            notifyHandlers();
        }

        return true;
    }

    @Override
    public boolean add(final T elem, final long bytes) throws IOException {
        return add(elem);
    }

    public void flush() throws IOException {

        sortIt();
        flushCount++;
        List<T> t = m_writer.writeList(m_list);
        if (t == null) {
            m_list = new ArrayList<T>();
        } else {
            m_list.clear();
        }
        m_map.clear();
    }

    private void sortIt() {
        mapToList();
        if (m_comp != null) {
            //maybe we want to order it differently.
            Collections.sort(m_list, m_comp);
        }
    }

    @Override
    public void setMaxBytes(final long bytes) {
        //To change body of implemented methods use File | Settings | File Templates.
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

    private void mapToList() {
        m_list.clear();
        Set<Map.Entry<T, T>> set = m_map.entrySet();
        for (Map.Entry<T, T> elem : set) {
            m_list.add(elem.getKey());
        }
    }

}
