package ht.util.servicecounters.registers;

import ht.util.servicecounters.CounterSet;
import ht.util.servicecounters.Register;

/**
 * Copyright (c) 2003 - present HiTorro All rights reserved. User: chris
 */
public class DoubleSubtractRegister extends LongSubtractRegister {
    public DoubleSubtractRegister(CounterSet cs, String name, String description, Register reg1, Register reg2) {
        super(cs, name, description, reg1, reg2);
    }

    public String getAsString(boolean prior, int i) {
        return Double.toString(getAsDouble(prior, i));
    }

    public String getValue() {
        return Double.toString(getAsDouble());
    }
}



