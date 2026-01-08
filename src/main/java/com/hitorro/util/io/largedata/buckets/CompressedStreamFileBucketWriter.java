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
import com.hitorro.util.io.largedata.BaseFileAccessingObjectFactory;
import com.hitorro.util.io.largedata.CompressedStreamIO;
import com.hitorro.util.io.largedata.CompressedStreamIteratorSink;
import org.apache.commons.collections.iterators.ArrayIterator;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

/**
 * <p/>
 */

public class CompressedStreamFileBucketWriter<T extends CompressedStreamIO> extends BaseFileBucketWriter<T> {
    public CompressedStreamFileBucketWriter(int maxLength, BaseFile directory, String extension, BaseFileAccessingObjectFactory factory) {
        super(maxLength, directory, extension, factory);
    }


    public int writeListAux(BaseFile temp, List<T> list) throws IOException {
        Iterator<T> iter = list.iterator();
        CompressedStreamIteratorSink<T> sink = new CompressedStreamIteratorSink<T>();
        sink.setIterator(iter);
        sink.setFile(temp);
        int count = sink.sink();
        return count;
    }

    public boolean add(T elem) throws IOException {
        return super.add(elem, elem.getSize());
    }

    public T[] writeArrayAux(BaseFile temp, T[] arr, int max) throws IOException {
        Iterator<T> iter = new ArrayIterator(arr, 0, max);
        CompressedStreamIteratorSink<T> sink = new CompressedStreamIteratorSink<T>();
        sink.setIterator(iter);
        sink.setFile(temp);
        int count = sink.sink();
        for (int i = 0; i < max; i++) {
            arr[i] = null;
        }
        return arr;
    }
}



