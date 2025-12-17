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

import com.hitorro.util.io.largedata.BaseFileAccessingObjectFactory;

import java.io.IOException;
import java.util.HashMap;

/**
 *
 */
public class HashWrappedArrayBucketWriterContainer<T> extends ArrayBucketWriterContainer<T> {
    private HashMap<T, T> map = new HashMap();

    public HashWrappedArrayBucketWriterContainer(int maxLength, BaseFileAccessingObjectFactory<T> tObjectFactory) {
        super(maxLength, tObjectFactory);
        map = new HashMap(maxLength);
    }

    public boolean add(T elem, long bytes) throws IOException {
        T old = map.get(elem);
        if (old == null) {
            map.put(elem, elem);
            return super.add(elem, bytes);
        } else {
            elem = m_merger.apply(old, elem);
            if (elem != old) {
                // try to avoid doing a put if we modified the one in the apply already.
                map.put(elem, elem);
                return super.add(elem, bytes);
            }
            return true;
        }
    }

    public void flush() throws IOException {
        super.flush();
        map.clear();
    }
}
