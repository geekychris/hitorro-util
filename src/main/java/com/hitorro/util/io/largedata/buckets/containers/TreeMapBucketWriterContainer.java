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
import com.hitorro.util.core.iterator.CollectionIterator;
import com.hitorro.util.core.iterator.LikeRowMerger;

import java.io.IOException;
import java.util.*;

/**

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
