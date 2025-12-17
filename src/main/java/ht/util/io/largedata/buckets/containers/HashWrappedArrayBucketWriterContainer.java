package ht.util.io.largedata.buckets.containers;

import ht.util.io.largedata.BaseFileAccessingObjectFactory;

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
