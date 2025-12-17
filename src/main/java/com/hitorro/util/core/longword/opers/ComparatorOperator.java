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
package com.hitorro.util.core.longword.opers;

import com.hitorro.util.core.CompOperEnum;
import com.hitorro.util.core.longword.NamedBitsOfLong;
import com.hitorro.util.core.longword.WordBits;

/**
 * Compare the value from the named bits of long against a constant value
 */
public class ComparatorOperator implements LongOperator {
    private CompOperEnum oper;
    private NamedBitsOfLong nbl;
    private long compValue;

    public ComparatorOperator(WordBits bits, String name, long compValue, CompOperEnum oper) {
        nbl = bits.get(name);
        this.oper = oper;
        this.compValue = compValue;
    }

    public ComparatorOperator(NamedBitsOfLong nbl, long compValue, CompOperEnum oper) {
        this.nbl = nbl;
        this.oper = oper;
        this.compValue = compValue;
    }

    @Override
    public boolean match(final long l) {
        return oper.isTrue(l, compValue);
    }

    @Override
    public void initForPass() {
    }
}
