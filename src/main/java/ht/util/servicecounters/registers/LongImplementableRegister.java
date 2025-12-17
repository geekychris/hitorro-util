package ht.util.servicecounters.registers;

import ht.util.servicecounters.CounterContext;
import ht.util.servicecounters.CounterSet;
import ht.util.servicecounters.Register;

/**
 * Allows
 */
public abstract class LongImplementableRegister extends Register {
    public LongImplementableRegister(final CounterSet cs, final String name, final String description) {
        super(cs, name, description);
        cs.addRegister(this);
        CounterContext.getContext().addRegister(cs, this);
    }

    public void clock(int depth) {
    }

    public double getAsDouble(boolean prior, int i) {
        return getAsDouble(prior, i);
    }

    public String getAsString(boolean prior, int i) {
        return Long.toString(getAsLong());
    }

    public double getAsDouble() {
        return getAsLong();
    }

    public String getValue() {
        return Long.toString(getAsLong());
    }

    public boolean isCascading() {
        return false;
    }

    public long getAsLong(boolean prior, int i) {
        return getAsLong();
    }
}
