package ht.util.servicecounters.registers;

import ht.util.servicecounters.CounterSet;
import ht.util.servicecounters.Register;

/**
 * Copyright (c) 2003 - present HiTorro All rights reserved. User: chris
 */
public class DoubleAddRegister extends LongAddRegister {
    public DoubleAddRegister(final CounterSet cs, final String name, final String description, final Register... regs) {
        super(cs, name, description, regs);
    }

    public String getAsString(boolean prior, int i) {
        return Double.toString(getAsDouble(prior, i));
    }

    public String getValue() {
        return Double.toString(getAsDouble());
    }
}
