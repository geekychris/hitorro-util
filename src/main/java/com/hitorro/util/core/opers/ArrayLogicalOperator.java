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
package com.hitorro.util.core.opers;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Given an array of some object.  see if any or all of its values test the embedded logical operator.
 */
public class ArrayLogicalOperator<F> implements HTPredicate<Object[]> {
    private HTPredicate<F> op;
    private Mode mode;

    public ArrayLogicalOperator(HTPredicate<F> op, Mode mode) {
        this.op = op;
        this.mode = mode;
    }

    @Override
    public void initForPass() {

    }

    @Override
    public boolean test(final Object e[]) {
        switch (mode) {
            case Any:
                for (Object o : e) {
                    if (op.test((F) o)) {
                        return true;
                    }
                }
                return false;
            case All:
                for (Object o : e) {
                    if (!op.test((F) o)) {
                        return false;
                    }
                }
                return true;
        }
        return false;
    }

    @Override
    public boolean initFromMap(final JsonNode map) {
        return false;
    }

    public enum Mode {
        All, Any
    }

}
