package ht.util.core.longword.opers;

import ht.util.core.CompOperEnum;
import ht.util.core.longword.NamedBitsOfLong;
import ht.util.core.longword.WordBits;

/**
 * Compare the value from the named bits of long against a constant value
 */
public class ComparatorOperator implements LongOperator {
    private CompOperEnum oper;
    private NamedBitsOfLong nbl;
    private long compValue;

    public ComparatorOperator(WordBits bits, String name, long compValue, CompOperEnum oper) {
        nbl = bits.get(name);
        this.oper = oper;
        this.compValue = compValue;
    }

    public ComparatorOperator(NamedBitsOfLong nbl, long compValue, CompOperEnum oper) {
        this.nbl = nbl;
        this.oper = oper;
        this.compValue = compValue;
    }

    @Override
    public boolean match(final long l) {
        return oper.isTrue(l, compValue);
    }

    @Override
    public void initForPass() {
    }
}
