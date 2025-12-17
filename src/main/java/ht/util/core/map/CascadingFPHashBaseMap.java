package ht.util.core.map;


import gnu.trove.map.hash.TLongLongHashMap;
import ht.jsontypesystem.JVS;
import ht.util.core.longword.WordBits;
import ht.util.core.longword.opers.LongOperator;

/**
 *
 */
public class CascadingFPHashBaseMap implements FPHashBaseMapInterface {
    private FPHashBaseMapInterface elems[];
    private int size;

    public CascadingFPHashBaseMap(FPHashBaseMapInterface elems[]) {
        this.elems = elems;
        size = elems.length;
    }

    @Override
    public TLongLongHashMap getMap(final int layer) {
        return elems[layer].getMap(layer);
    }

    @Override
    public int contains(final long key) {
        if (elems[0].contains(key) != -1) {
            return 0;
        }
        return containsAux(key, 1);
    }

    private int containsAux(final long key, int depth) {
        if (depth >= size) {
            return -1;
        }
        if (elems[depth].contains(key) != -1) {
            return depth;
        }
        return containsAux(key, depth + 1);

    }

    @Override
    public int match(final String key, final LongOperator oper, final int layer) {
        return elems[layer].match(key, oper, layer);
    }

    @Override
    public int match(final long fp, final LongOperator oper, final int layer) {
        return elems[layer].match(fp, oper, layer);
    }

    @Override
    public long get(final String key, final int layer) {
        return elems[layer].get(key, layer);
    }

    @Override
    public long get(final long fp, final int layer) {
        return elems[layer].get(fp, layer);
    }

    @Override
    public WordBits getWordBits(final int layer) {
        return elems[layer].getWordBits(layer);
    }

    public void updateRecord(final JVS m, final String v, int layer) {
        elems[layer].updateRecord(m, v, layer);
    }
}
