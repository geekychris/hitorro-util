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

import com.hitorro.util.servicecounters.CounterContext;
import com.hitorro.util.servicecounters.CounterSet;
import com.hitorro.util.servicecounters.Register;

/**
 *
 */
public class LongRegister extends Register {
    private volatile long[] registers;
    private volatile long[] registersPrior;
    private volatile long register;

    public LongRegister(CounterSet cs, final String name, final String description) {
        super(cs, name, description);
        registers = new long[CounterContext.getContext().getRawRegisterCount()];
        registersPrior = new long[CounterContext.getContext().getRawRegisterCount()];
    }

    public void tick() {
        incrementBy(1);
    }

    public void clock(int depth) {
        for (int i = 0; i <= depth; i++) {
            registers[i + 1] += registers[i];
            registersPrior[i] = registers[i];
            registers[i] = 0;
        }
    }

    public void incrementBy(long v) {
        registers[0] += v;
        register += v;
    }

    public void decrementBy(long v) {
        registers[0] -= v;
        register -= v;
    }

    public void setTo(long v) {
        registers[0] = v;
        register = v;
    }

    public long get() {
        return register;
    }

    public double getAsDouble(boolean prior, int i) {
        if (prior) {
            return registersPrior[i];
        }
        return registers[i];
    }

    public long getAsLong(boolean prior, int i) {
        if (prior) {
            return registersPrior[i];
        }
        return registers[i];
    }

    public String getAsString(boolean prior, int i) {
        if (prior) {
            return Long.toString(registersPrior[i]);
        }
        return Long.toString(registers[i]);
    }

    public double getAsDouble() {
        return register;
    }

    public long getAsLong() {
        return register;
    }

    public String getValue() {
        return Long.toString(register);
    }
}
