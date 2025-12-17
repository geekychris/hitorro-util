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
public class DoubleRegister extends Register {
    private volatile double[] registers;
    private volatile double[] registersPrior;
    private volatile double register;

    public DoubleRegister(CounterSet cs, final String name, String description) {
        super(cs, name, description);
        registers = new double[CounterContext.getContext().getRawRegisterCount()];
        registersPrior = new double[CounterContext.getContext().getRawRegisterCount()];
    }

    public void clock(int depth) {
        for (int i = 0; i <= depth; i++) {
            registers[i + 1] += registers[i];
            registersPrior[i] = registers[i];
            registers[i] = 0;
        }
    }

    public void incrementBy(double v) {
        registers[0] += v;
        register += v;
    }

    public void decrementBy(double v) {
        registers[0] -= v;
        register -= v;
    }

    public void setTo(double v) {
        registers[0] = v;
        register = v;
    }

    public double getAsDouble(boolean prior, int i) {
        if (prior) {
            return registersPrior[i];
        }
        return registers[i];
    }

    public long getAsLong(boolean prior, int i) {
        if (prior) {
            return (long) registersPrior[i];
        }
        return (long) registers[i];
    }

    public String getAsString(boolean prior, int i) {
        if (prior) {
            return Double.toString(registersPrior[i]);
        }
        return Double.toString(registers[i]);
    }

    public double getAsDouble() {
        return register;
    }

    public long getAsLong() {
        return (long) register;
    }

    public String getValue() {
        return Double.toString(register);
    }
}
