package ht.util.io.largedata.iterator;

import ht.util.basefile.fs.BaseFile;
import ht.util.core.Log;
import ht.util.core.iterator.AbstractIterator;
import ht.util.core.iterator.sinks.Sink;
import ht.util.core.string.Fmt;
import ht.util.io.largedata.BaseFileAccessingObjectFactory;

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
    protected Queue<BaseFile> m_deleteQueue = new LinkedList<BaseFile>();
    protected boolean m_deleteOnceMerged;
    protected String m_finalFileExtension;
    protected BaseFileAccessingObjectFactory<E> factory;
    private int m_noDeleteCount = -1;

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
            m_noDeleteCount = fList.length;
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
                        while (m_deleteQueue.size() > 0) {
                            BaseFile fd = m_deleteQueue.remove();
                            if (m_noDeleteCount > 0) {
                                // we potentially dont want to delete the input element.
                                m_noDeleteCount--;
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
        m_deleteQueue.clear();
        for (int i = 0; i < count; i++) {
            BaseFile fc = m_queue.remove();
            ptsArray[i] = getIterator(fc);
            m_deleteQueue.add(fc);
        }
        return ptsArray;
    }

    protected AbstractIterator<E> getIterator(BaseFile bf) throws IOException {
        return factory.getBaseFileToChainingMapper().apply(bf);
    }
}
