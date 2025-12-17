package ht.util.core.longword.opers;

import ht.util.core.longword.NamedBitsOfLong;
import ht.util.core.longword.WordBits;

/**
 * Naive bit test against specific bit of a longword.
 */
public class BitSetOperator implements LongOperator {
    private NamedBitsOfLong nbl;

    public BitSetOperator(WordBits bits, String name) {
        nbl = bits.get(name);
    }

    public BitSetOperator(NamedBitsOfLong nbl) {
        this.nbl = nbl;
    }

    public boolean match(long l) {
        return nbl.isSet(l);
    }

    public void initForPass() {

    }
}
