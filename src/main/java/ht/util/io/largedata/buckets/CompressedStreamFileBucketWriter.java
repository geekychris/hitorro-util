package ht.util.io.largedata.buckets;

import ht.util.basefile.fs.BaseFile;
import ht.util.io.largedata.BaseFileAccessingObjectFactory;
import ht.util.io.largedata.CompressedStreamIO;
import ht.util.io.largedata.CompressedStreamIteratorSink;
import org.apache.commons.collections.iterators.ArrayIterator;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

/**
 * <p/>
 * Copyright (c) 2003 - present HiTorro All rights reserved. User: chris Date: Dec 31, 2005 Time: 10:18:04 AM
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



