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
package com.hitorro.util.servicecounters.registers;

import com.hitorro.util.servicecounters.CounterSet;
import com.hitorro.util.servicecounters.Register;

/**
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



