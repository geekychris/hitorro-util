package ht.util.core.longword.opers;

import ht.util.core.longword.NamedBitsOfLong;
import ht.util.core.longword.WordBits;

/**
 * Check bits are equal, gt gte, lt, lte.
 */
public class BitsEqual implements LongOperator {
    private NamedBitsOfLong nbl;
    private Oper oper;
    private long val;
    public BitsEqual(WordBits bits, String name, long val, Oper oper) {
        nbl = bits.get(name);
        this.oper = oper;
        this.val = val;
    }

    public BitsEqual(NamedBitsOfLong nbl, long val, Oper oper) {
        this.nbl = nbl;
        this.val = val;
        this.oper = oper;
    }

    public boolean match(long l) {
        long v = nbl.get(l);
        switch (oper) {
            case Equal:
                return v == val;
            case LessThan:
                return v < val;
            case LessThanEqual:
                return v <= val;
            case GreaterThan:
                return v > val;
            case GreaterThanEqual:
                return v >= val;
        }
        return false;
    }

    public void initForPass() {

    }

    public enum Oper {
        Equal, LessThan, LessThanEqual, GreaterThan, GreaterThanEqual
    }
}

