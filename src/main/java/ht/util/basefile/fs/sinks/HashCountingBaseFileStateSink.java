package ht.util.basefile.fs.sinks;


import com.fasterxml.jackson.databind.JsonNode;
import gnu.trove.map.hash.TObjectIntHashMap;
import ht.util.basefile.fs.BaseFile;
import ht.util.io.StoreException;

import java.io.IOException;

/**
 *
 */
public abstract class HashCountingBaseFileStateSink<E> extends BaseFileStatsSink<E> {
    protected TObjectIntHashMap<E> set = new TObjectIntHashMap();

    public HashCountingBaseFileStateSink(BaseFile outputFile) {
        super(outputFile);
    }

    @Override
    public boolean init(JsonNode node) {
        return true;
    }

    @Override
    public boolean start() {
        set.clear();
        return true;
    }

    @Override
    public boolean add(final E o) throws IOException, StoreException {
        if (o == null) {
            // dont store nulls
            return true;
        }
        if (set.contains(o)) {
            set.increment(o);
        } else {
            set.put(o, 1);
        }
        return true;
    }
}
