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
package com.hitorro.util.io.largedata.buckets;

import com.hitorro.util.basefile.fs.BaseFile;
import com.hitorro.util.core.Log;
import com.hitorro.util.core.iterator.Mapper;
import com.hitorro.util.core.iterator.sinks.Sink;
import com.hitorro.util.core.string.Fmt;
import com.hitorro.util.io.StoreException;
import com.hitorro.util.io.largedata.BaseFileAccessingObjectFactory;
import org.apache.commons.collections.iterators.ArrayIterator;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

/**
 *
 */
public class BaseFileBucketWriter<T> extends BaseBucketWriter<T> {
    protected BaseFile m_directory;
    protected String m_extension;
    protected long fileCounter = 0;
    protected Mapper<BaseFile, Sink<T>> mapper;
    protected BaseFileAccessingObjectFactory<T> objFactory;

    /**
     * If maxLength == -1 then we are in a single file mode, we assume we can manage the listFiles in memory.
     *
     * @param maxSize
     * @param directory
     * @param extension
     */

    public BaseFileBucketWriter(int maxSize,
                                BaseFile directory,
                                String extension,
                                BaseFileAccessingObjectFactory<T> fact) {
        super(maxSize, fact);
        objFactory = fact;
        m_directory = directory;
        m_extension = extension;
        this.mapper = fact.getBaseFileToSinkMapper();
    }

    public List writeList(List<T> list) throws IOException {
        if (list.size() == 0) {
            // nothing to see here - move on!
            return list;
        }

        m_directory.mkdir();
        long time = System.currentTimeMillis();
        BaseFile temp;
        if (m_extension.endsWith("gz")) {
            temp = m_directory.getChild(Fmt.S("%s-%s.tmp.gz", time, fileCounter++));
        } else {
            temp = m_directory.getChild(Fmt.S("%s-%s.tmp", time, fileCounter++));
        }

        int count = writeListAux(temp, list);
        BaseFile f = m_directory.getChild(Fmt.S("%s-%s.%s", time, count, m_extension));
        temp.renameTo(f);
        return list;

    }

    public T[] writeArray(T arr[], int maxIndex) throws IOException {
        if (maxIndex == 0) {
            // nothing to see here - move on!
            return arr;
        }

        m_directory.mkdir();
        long time = System.currentTimeMillis();
        BaseFile temp = m_directory.getChild(Fmt.S("%s-%s.tmp", time, fileCounter++));

        T[] ret = writeArrayAux(temp, arr, maxIndex + 1);

        BaseFile f = m_directory.getChild(Fmt.S("%s-%s.%s", time, maxIndex, m_extension));
        temp.renameTo(f);
        return arr;
    }

    public int writeListAux(BaseFile temp, List<T> list) throws IOException {
        Iterator<T> iter = list.iterator();
        int counter = sink(temp, iter);
        return counter;
    }

    private int sink(final BaseFile temp, final Iterator<T> iter) throws IOException {
        Sink<T> sink = mapper.apply(temp);
        sink.start();
        int counter = 0;
        try {
            while (iter.hasNext()) {
                counter++;
                sink.add(iter.next());
            }
        } catch (StoreException e) {
            Log.io.error("Unable to write to put %s %e", e, e);
            return -1;
        }
        sink.stop();
        return counter;
    }

    public T[] writeArrayAux(BaseFile temp, T[] arr, int max) throws IOException {
        Iterator<T> iter = new ArrayIterator(arr, 0, max);
        int count = sink(temp, iter);
        for (int i = 0; i < max; i++) {
            arr[i] = null;
        }
        return arr;
    }
}