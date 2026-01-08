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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


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
