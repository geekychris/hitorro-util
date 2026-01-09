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
package com.hitorro.util.io.largedata.iterator;

import com.hitorro.util.basefile.fs.BaseFile;
import com.hitorro.util.core.Log;
import com.hitorro.util.core.iterator.AbstractIterator;
import com.hitorro.util.core.iterator.sinks.Sink;
import com.hitorro.util.core.string.Fmt;
import com.hitorro.util.io.largedata.BaseFileAccessingObjectFactory;

import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Select tree that deligates all the construction of readers and writers to the factory class
 */
public class BaseFileSelectTreeController<E> {
    protected int m_maxPerIter;
    protected BaseFile m_dir;
    protected Queue<BaseFile> m_queue = new LinkedList<BaseFile>();
    protected Queue<BaseFile> deleteQueue = new LinkedList<BaseFile>();
    protected boolean m_deleteOnceMerged;
    protected String m_finalFileExtension;
    protected BaseFileAccessingObjectFactory<E> factory;
    private int noDeleteCount = -1;

    public BaseFileSelectTreeController(BaseFile dir,
                                        BaseFile fList[],
                                        int maxPerIter,
                                        BaseFileAccessingObjectFactory factory,
                                        boolean deleteOnceMerged,
                                        String finalFileExtension,
                                        boolean removeOriginalFiles) throws IOException {
        init(finalFileExtension, dir, maxPerIter, factory, deleteOnceMerged, fList, removeOriginalFiles);
    }

    private void init(final String finalFileExtension,
                      final BaseFile dir,
                      final int maxPerIter,
                      final BaseFileAccessingObjectFactory<E> factory,
                      final boolean deleteOnceMerged,
                      final BaseFile[] fList,
                      boolean removeOriginalFiles) throws IOException {
        if (!removeOriginalFiles) {
            // when we dont want to remove the original files, we track a high water mark in the queue of the
            // files we passed in, only removing the merged files we create in the apply process
            noDeleteCount = fList.length;
        }
        m_finalFileExtension = finalFileExtension;
        m_dir = dir;
        m_maxPerIter = maxPerIter;
        this.factory = factory;
        m_deleteOnceMerged = deleteOnceMerged;
        for (BaseFile f : fList) {
            if (f.missingOrEmpty()) {
                if (!f.exists()) {
                    Log.util.warn("File passed to select tree controller didn't exist: %s", f);
                } else if (f.length() == 0 && removeOriginalFiles) {
                    f.delete();
                } else if (f.length() == 0) {
                    Log.util.warn("Empty file passed to select tree controller: %s", f);
                }
            } else {
                m_queue.add(f);
            }
        }
    }

    public BaseFile merge() throws Exception {
        int counter = 0;
        BaseFile f = null;
        if (m_queue.size() == 0) {
            return null;
        }
        if (m_queue.size() > 1) {
            AbstractIterator<E> sti = null;
            Queue<BaseFile> nextQueue = new LinkedList<BaseFile>();
            while (!(m_queue.size() == 1 && nextQueue.size() == 0)) {
                int s = m_queue.size();

                if (s == 0) {
                    m_queue = nextQueue;
                    nextQueue = new LinkedList<BaseFile>();
                    continue;
                } else if (s == 1) {
                    nextQueue.add(m_queue.remove());
                    m_queue = nextQueue;
                    nextQueue = new LinkedList<BaseFile>();
                    continue;
                }

                try {
                    AbstractIterator[] inputIteratorArray = getInputIterators();

                    if (inputIteratorArray.length == 1) {
                        sti = inputIteratorArray[0];
                    } else {
                        sti = new SelectionTreeIterator(factory.getDefaultComparitor(), inputIteratorArray);
                    }
                    sti = sti.removing(factory.getDefaultComparitor(), factory.getRowMerger());


                    f = m_dir.getChild(Fmt.S("%s-%s.%s", System.currentTimeMillis(), counter++, m_finalFileExtension));
                    Sink<E> sink = factory.getBaseFileToSinkMapper().apply(f);

                    int count = sti.sink(sink);
                    nextQueue.add(f);
                    if (this.m_deleteOnceMerged) {
                        while (deleteQueue.size() > 0) {
                            BaseFile fd = deleteQueue.remove();
                            if (noDeleteCount > 0) {
                                // we potentially dont want to delete the input element.
                                noDeleteCount--;
                            } else {
                                if (!fd.delete()) {
                                    Log.util.error("Unable to delete merged file: %s", fd);
                                }
                            }
                        }
                    }
                } finally {
                    // close the select tree
                    if (sti != null) {
                        if (sti instanceof AutoCloseable) {
                            sti.close();
                        }
                        sti = null;
                    }
                }
            }
        } else {
            // we only had one item on the queue anyway.
            f = m_queue.remove();
        }
        return f;
    }

    protected AbstractIterator[] getInputIterators() throws IOException {
        int count = Math.min(m_maxPerIter, m_queue.size());
        AbstractIterator[] ptsArray = new AbstractIterator[count];
        deleteQueue.clear();
        for (int i = 0; i < count; i++) {
            BaseFile fc = m_queue.remove();
            ptsArray[i] = getIterator(fc);
            deleteQueue.add(fc);
        }
        return ptsArray;
    }

    protected AbstractIterator<E> getIterator(BaseFile bf) throws IOException {
        return factory.getBaseFileToChainingMapper().apply(bf);
    }
}
