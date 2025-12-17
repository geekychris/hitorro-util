package ht.util.servicecounters.registers;

import ht.util.servicecounters.CounterSet;
import ht.util.servicecounters.Register;

/**
 * Copyright (c) 2003 - present HiTorro All rights reserved. User: chris
 */
public class LongSubtractRegister extends Register {
    Register reg1;
    Register reg2;

    public LongSubtractRegister(CounterSet cs, String name, String description, Register reg1, Register reg2) {
        super(cs, name, description);
        this.reg1 = reg1;
        this.reg2 = reg2;
    }

    public void clock(int depth) {
        // do nothing
    }

    public double getAsDouble(boolean prior, int i) {
        return reg1.getAsDouble(prior, i) - reg2.getAsDouble(prior, i);
    }

    public long getAsLong(boolean prior, int i) {
        return reg1.getAsLong(prior, i) - reg2.getAsLong(prior, i);
    }

    public String getAsString(boolean prior, int i) {
        return Long.toString(getAsLong(prior, i));
    }

    public double getAsDouble() {
        return reg1.getAsDouble() - reg2.getAsDouble();
    }

    public long getAsLong() {
        return reg1.getAsLong() - reg2.getAsLong();
    }

    public String getValue() {
        return Long.toString(getAsLong());
    }
}



