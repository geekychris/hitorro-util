package ht.util.io.largedata;

import ht.util.basefile.fs.BaseFile;
import ht.util.io.largedata.iterator.SelectTreeIteratorConstructor;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

/**
 * Construct Compressed Stream for the select tree iterator constructor
 */
public class CompressedStreamSelectTreeConstructor<T extends CompressedStreamIO> extends SelectTreeIteratorConstructor {
    protected BaseFileAccessingObjectFactory<T> factory;

    public CompressedStreamSelectTreeConstructor(BaseFileAccessingObjectFactory<T> fact) {
        factory = fact;
    }

    protected Iterator getIterator(File fc) throws IOException {
        return new CompressedStreamIOIterator(fc, factory);
    }

    protected Iterator getIterator(BaseFile fc) throws IOException {
        return new CompressedStreamIOIterator(fc, factory);
    }

    protected Iterator getIterator(InputStream is) throws IOException {
        return new CompressedStreamIOIterator(is, factory);
    }
}
