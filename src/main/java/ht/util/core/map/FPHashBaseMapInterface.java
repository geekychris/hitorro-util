package ht.util.core.map;


import gnu.trove.map.hash.TLongLongHashMap;
import ht.jsontypesystem.JVS;
import ht.util.core.longword.WordBits;
import ht.util.core.longword.opers.LongOperator;

/**
 *
 */
public interface FPHashBaseMapInterface {
    TLongLongHashMap getMap(int layer);

    int contains(long key);

    int match(String key, LongOperator oper, int layer);

    int match(long fp, LongOperator oper, int layer);

    long get(String key, int layer);

    long get(long fp, int layer);

    WordBits getWordBits(int layer);


    /**
     * @param m - apply for the wordbit name to value mapping
     * @param v - token to update
     */
    void updateRecord(final JVS m, final String v, int layer);
}
