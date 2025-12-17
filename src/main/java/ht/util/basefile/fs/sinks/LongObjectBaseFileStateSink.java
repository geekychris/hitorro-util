package ht.util.basefile.fs.sinks;

import com.fasterxml.jackson.databind.JsonNode;
import gnu.trove.map.hash.TLongObjectHashMap;
import ht.util.basefile.fs.BaseFile;

/**
 *
 */
public abstract class LongObjectBaseFileStateSink<E, S> extends BaseFileStatsSink<E> {
    protected TLongObjectHashMap<S> map;

    public LongObjectBaseFileStateSink() {
        super(null);
    }

    public LongObjectBaseFileStateSink(BaseFile outputFile, int size) {
        super(outputFile);
        map = new TLongObjectHashMap(size);
    }

    @Override
    public boolean init(JsonNode node) {
        return true;
    }

    @Override
    public boolean start() {
        map.clear();
        return true;
    }
}