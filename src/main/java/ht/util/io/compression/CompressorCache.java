package ht.util.io.compression;

import gnu.trove.map.hash.TIntObjectHashMap;
import ht.util.core.thread.ThreadStash;

import java.io.IOException;

public class CompressorCache {
    private static ThreadStash<TIntObjectHashMap<Compressor>> threadedCompressors = new ThreadStash<TIntObjectHashMap<Compressor>>() {
        @Override
        public TIntObjectHashMap<Compressor> getNew() {
            return new TIntObjectHashMap();
        }
    };

    public static synchronized Compressor get(int version) throws IOException {
        Compressor c = threadedCompressors.get().get(version);
        if (c == null) {
            c = new DictCompressor();
            c.init(version);
            threadedCompressors.get().put(version, c);
        }
        return c;
    }
}
