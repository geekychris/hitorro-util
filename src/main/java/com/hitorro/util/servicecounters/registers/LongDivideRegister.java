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


public class LongDivideRegister extends Register {
    Register dividend;
    Register divisor;

    public LongDivideRegister(CounterSet cs, String name, String description, Register dividend, Register divisor) {
        super(cs, name, description);
        this.dividend = dividend;
        this.divisor = divisor;
    }

    public void clock(int depth) {
        // do nothing
    }

    public double getAsDouble(boolean prior, int i) {
        return dividend.getAsLong(prior, i) / divisor.getAsLong(prior, i);
    }

    public long getAsLong(boolean prior, int i) {
        return getAsLong(prior, i);
    }

    public String getAsString(boolean prior, int i) {
        return Double.toString(getAsLong(prior, i));
    }

    public double getAsDouble() {
        return dividend.getAsLong() / divisor.getAsLong();
    }

    public long getAsLong() {
        return dividend.getAsLong() / divisor.getAsLong();
    }

    public String getValue() {
        return Long.toString(dividend.getAsLong() / divisor.getAsLong());
    }
}

