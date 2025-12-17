package ht.util.io.largedata;

import com.fasterxml.jackson.databind.JsonNode;
import ht.util.basefile.fs.BaseFile;
import ht.util.core.Log;
import ht.util.core.iterator.AbstractIterator;
import ht.util.core.iterator.Mapper;

import java.io.IOException;

/**
 * Created by IntelliJ IDEA. User: Chris Date: 8/21/11 Time: 9:56 PM To change this template use File | Settings | File
 * Templates.
 */
public class CompressedIOIteratorMapper<T> implements Mapper<BaseFile, AbstractIterator<T>> {
    private BaseFileAccessingObjectFactory factory;

    public CompressedIOIteratorMapper(BaseFileAccessingObjectFactory factory) {
        this.factory = factory;
    }

    public String initPass(final JsonNode map) {
        return null;
    }

    public AbstractIterator<T> apply(final BaseFile bf) {
        try {
            return new CompressedStreamIOIterator(bf, factory);
        } catch (IOException e1) {
            Log.filesystem.error("Unable to open input file %s error %s %e", bf, e1, e1);
        }
        return null;
    }
}


