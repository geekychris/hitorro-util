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

import ht.util.servicecounters.CounterContext;
import ht.util.servicecounters.CounterSet;
import ht.util.servicecounters.Register;

/**
 * Allows
 */
public abstract class LongImplementableRegister extends Register {
    public LongImplementableRegister(final CounterSet cs, final String name, final String description) {
        super(cs, name, description);
        cs.addRegister(this);
        CounterContext.getContext().addRegister(cs, this);
    }

    public void clock(int depth) {
    }

    public double getAsDouble(boolean prior, int i) {
        return getAsDouble(prior, i);
    }

    public String getAsString(boolean prior, int i) {
        return Long.toString(getAsLong());
    }

    public double getAsDouble() {
        return getAsLong();
    }

    public String getValue() {
        return Long.toString(getAsLong());
    }

    public boolean isCascading() {
        return false;
    }

    public long getAsLong(boolean prior, int i) {
        return getAsLong();
    }
}
