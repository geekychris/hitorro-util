package ht.util.servicecounters.registers;

import ht.util.servicecounters.CounterContext;
import ht.util.servicecounters.CounterSet;
import ht.util.servicecounters.Register;

/**
 *
 */
public abstract class DoubleImplementableRegister extends Register {
    public DoubleImplementableRegister(final CounterSet cs, final String name, final String description) {
        super(cs, name, description);
        cs.addRegister(this);
        CounterContext.getContext().addRegister(cs, this);
    }

    public void clock(int depth) {
    }

    public String getAsString(boolean prior, int i) {
        return Double.toString(getAsDouble());
    }

    public long getAsLong(boolean prior, int i) {
        return (long) getAsDouble();
    }

    public long getAsLong() {
        return (long) getAsDouble();
    }

    public String getValue() {
        return Double.toString(getAsDouble());
    }

    public boolean isCascading() {
        return false;
    }
}


