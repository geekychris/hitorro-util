package ht.util.core.map;


import gnu.trove.map.hash.TLongLongHashMap;
import ht.jsontypesystem.JVS;
import ht.util.core.GenericKeyValue;
import ht.util.core.iterator.mappers.BaseMapper;
import ht.util.core.longword.WordBits;
import ht.util.core.longword.opers.LongOperator;

import java.util.List;

/**
 *
 */
public class FPHashBaseMap implements FPHashBaseMapInterface {
    public static final int defaultInitialSize = 1000000;
    protected TLongLongHashMap map;
    protected WordBits bits;
    protected BaseMapper<String, Long> keyMapping;
    protected int layer;

    public void init(int layer, WordBits bits, int initialSize, BaseMapper<String, Long> keyMapping, boolean lowerCase) {
        this.layer = layer;
        map = new TLongLongHashMap(initialSize);
        this.bits = bits;
        this.keyMapping = keyMapping;
    }

    public int getLayer() {
        return layer;
    }

    public FPHashBaseMap setPhrases(List<GenericKeyValue<String, Integer>> tokens, String wordBitName) {
        return this;
    }

    public TLongLongHashMap getMap(int layer) {
        return map;
    }

    public int contains(long key) {
        if (map.contains(key)) {
            return layer;
        }
        return -1;
    }

    public int match(String key, LongOperator oper, int layer) {
        if (oper.match(get(key, layer))) {
            return layer;
        }
        return -1;
    }

    public int match(long fp, LongOperator oper, int layer) {
        if (oper.match(get(fp, layer))) {
            return layer;
        }
        return -1;
    }

    public long get(String key, int layer) {
        long fp = getHash(key);
        return map.get(fp);
    }

    public long get(long fp, int layer) {
        return map.get(fp);
    }

    public WordBits getWordBits(int layer) {
        return bits;
    }

    protected long getHash(String v) {
        return keyMapping.apply(v);
    }

    /**
     * @param m - apply for the wordbit name to value mapping
     * @param v - token to update
     */
    public void updateRecord(final JVS m, final String v, int layer) {
        long fp = getHash(v);
        long value = map.get(fp);
        value = bits.setFromMap(m, value);
        map.put(fp, value);
    }
}
