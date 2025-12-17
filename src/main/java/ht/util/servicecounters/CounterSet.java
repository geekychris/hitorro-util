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
package ht.util.servicecounters;

import ht.util.servicecounters.registers.*;

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

    public LongRegister getLongRegister(String name, String description) {
        LongRegister lr = new LongRegister(this, name, description);
        registers.add(lr);
        return lr;
    }

    public DoubleRegister getDoubleRegister(String name, String description) {
        DoubleRegister lr = new DoubleRegister(this, name, description);
        registers.add(lr);
        return lr;
    }

    public DoubleDivideRegister getDoubleDivideRegister(String name, String description, Register dividend, Register divisor) {
        DoubleDivideRegister lr = new DoubleDivideRegister(this, name, description, dividend, divisor);
        registers.add(lr);
        return lr;
    }

    public LongDivideRegister getLongDivideRegister(String name, String description, Register dividend, Register divisor) {
        LongDivideRegister lr = new LongDivideRegister(this, name, description, dividend, divisor);
        registers.add(lr);
        return lr;
    }

    public LongAddRegister getLongAddRegister(String name, String description, Register... regs) {
        LongAddRegister lr = new LongAddRegister(this, name, description, regs);
        addRegister(lr);
        return lr;
    }

    public DoubleAddRegister getDoubleAddRegister(String name, String description, Register... regs) {
        DoubleAddRegister lr = new DoubleAddRegister(this, name, description, regs);
        addRegister(lr);
        return lr;
    }

    public DoubleSubtractRegister getDoubleSubtractRegister(String name, String description, Register reg1, Register reg2) {
        DoubleSubtractRegister lr = new DoubleSubtractRegister(this, name, description, reg1, reg2);
        addRegister(lr);
        return lr;
    }

    public LongSubtractRegister getLongSubtractRegister(String name, String description, Register reg1, Register reg2) {
        LongSubtractRegister lr = new LongSubtractRegister(this, name, description, reg1, reg2);
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
