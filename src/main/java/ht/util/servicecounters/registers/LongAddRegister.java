package ht.util.servicecounters.registers;

import ht.util.servicecounters.CounterSet;
import ht.util.servicecounters.Register;

/**
 * Copyright (c) 2003 - present HiTorro All rights reserved. User: chris
 */
public class LongAddRegister extends Register {
    Register regs[];

    public LongAddRegister(CounterSet cs, String name, String description, Register... regs) {
        super(cs, name, description);
        this.regs = regs;
    }

    public void clock(int depth) {
        // do nothing
    }

    public double getAsDouble(boolean prior, int i) {

        double l = 0;
        for (Register r : regs) {
            l += r.getAsDouble(prior, i);
        }
        return i;
    }

    public long getAsLong(boolean prior, int i) {
        long l = 0;
        for (Register r : regs) {
            l += r.getAsLong(prior, i);
        }
        return i;
    }

    public String getAsString(boolean prior, int i) {
        return Long.toString(getAsLong(prior, i));
    }

    public double getAsDouble() {
        double l = 0;
        for (Register r : regs) {
            l += r.getAsDouble();
        }
        return l;
    }

    public long getAsLong() {
        long l = 0;
        for (Register r : regs) {
            l += r.getAsLong();
        }
        return l;
    }

    public String getValue() {
        return Long.toString(getAsLong());
    }
}



