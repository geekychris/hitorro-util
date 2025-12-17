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
package com.hitorro.util.servicecounters;

import com.hitorro.util.servicecounters.registers.*;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class CounterSet {
    private String name;

    private List<Register> registers = new ArrayList();

    public CounterSet(String name) {
        this.name = name;
    }

    public void clock(int i) {
        for (Register r : registers) {
            r.clock(i);
        }
    }

    public void addRegister(Register r) {
        registers.add(r);
    }

    public com.hitorro.util.servicecounters.registers.LongRegister getLongRegister(String name, String description) {
        com.hitorro.util.servicecounters.registers.LongRegister lr = new com.hitorro.util.servicecounters.registers.LongRegister(this, name, description);
        registers.add(lr);
        return lr;
    }

    public com.hitorro.util.servicecounters.registers.DoubleRegister getDoubleRegister(String name, String description) {
        com.hitorro.util.servicecounters.registers.DoubleRegister lr = new com.hitorro.util.servicecounters.registers.DoubleRegister(this, name, description);
        registers.add(lr);
        return lr;
    }

    public com.hitorro.util.servicecounters.registers.DoubleDivideRegister getDoubleDivideRegister(String name, String description, Register dividend, Register divisor) {
        com.hitorro.util.servicecounters.registers.DoubleDivideRegister lr = new com.hitorro.util.servicecounters.registers.DoubleDivideRegister(this, name, description, dividend, divisor);
        registers.add(lr);
        return lr;
    }

    public com.hitorro.util.servicecounters.registers.LongDivideRegister getLongDivideRegister(String name, String description, Register dividend, Register divisor) {
        com.hitorro.util.servicecounters.registers.LongDivideRegister lr = new com.hitorro.util.servicecounters.registers.LongDivideRegister(this, name, description, dividend, divisor);
        registers.add(lr);
        return lr;
    }

    public com.hitorro.util.servicecounters.registers.LongAddRegister getLongAddRegister(String name, String description, Register... regs) {
        com.hitorro.util.servicecounters.registers.LongAddRegister lr = new com.hitorro.util.servicecounters.registers.LongAddRegister(this, name, description, regs);
        addRegister(lr);
        return lr;
    }

    public com.hitorro.util.servicecounters.registers.DoubleAddRegister getDoubleAddRegister(String name, String description, Register... regs) {
        com.hitorro.util.servicecounters.registers.DoubleAddRegister lr = new com.hitorro.util.servicecounters.registers.DoubleAddRegister(this, name, description, regs);
        addRegister(lr);
        return lr;
    }

    public com.hitorro.util.servicecounters.registers.DoubleSubtractRegister getDoubleSubtractRegister(String name, String description, Register reg1, Register reg2) {
        com.hitorro.util.servicecounters.registers.DoubleSubtractRegister lr = new com.hitorro.util.servicecounters.registers.DoubleSubtractRegister(this, name, description, reg1, reg2);
        addRegister(lr);
        return lr;
    }

    public com.hitorro.util.servicecounters.registers.LongSubtractRegister getLongSubtractRegister(String name, String description, Register reg1, Register reg2) {
        com.hitorro.util.servicecounters.registers.LongSubtractRegister lr = new com.hitorro.util.servicecounters.registers.LongSubtractRegister(this, name, description, reg1, reg2);
        addRegister(lr);
        return lr;
    }

    public void finishInit(CounterContext cc) {
        cc.addCounterSet(this);
        for (Register r : registers) {
            cc.addRegister(this, r);
        }
    }

    public String getName() {
        return name;
    }
}
