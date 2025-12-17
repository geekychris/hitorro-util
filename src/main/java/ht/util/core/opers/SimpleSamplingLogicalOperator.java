package ht.util.core.opers;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Take every nth item that the operator comes across.
 */
public class SimpleSamplingLogicalOperator<E> implements HTPredicate<E> {
    private long counter = 0;
    private int mod;

    public SimpleSamplingLogicalOperator(int mod) {
        this.mod = mod;
    }

    @Override
    public void initForPass() {
    }

    @Override
    public boolean test(final E e) {
        return counter++ % mod == 0;
    }

    @Override
    public boolean initFromMap(final JsonNode map) {
        return true;
    }
}
