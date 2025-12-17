package ht.util.io.largedata;

import ht.util.basefile.fs.BaseFile;
import ht.util.core.Log;
import ht.util.core.iterator.Mapper;
import ht.util.core.iterator.sinks.Sink;

import java.io.IOException;

/**
 *
 */
public class CompressedIOSinkMapper<T extends CompressedStreamIO> implements Mapper<BaseFile, Sink<T>> {
    private BaseFileAccessingObjectFactory factory;

    public CompressedIOSinkMapper(BaseFileAccessingObjectFactory factory) {
        this.factory = factory;
    }

    @Override
    public Sink<T> apply(final BaseFile bf) {
        try {
            return new CompressedIOSink<T>(bf, factory);
        } catch (IOException e) {
            Log.filesystem.error("Unable to get CompressedIOSink for basefile %s error %s %e", bf, e, e);
            return null;
        }
    }
}
