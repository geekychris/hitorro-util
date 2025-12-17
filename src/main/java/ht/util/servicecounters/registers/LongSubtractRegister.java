/*
 * Copyright (c) 2006-2025 Chris Collins
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
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



