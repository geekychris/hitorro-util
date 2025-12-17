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

import com.fasterxml.jackson.databind.JsonNode;
import com.hitorro.util.core.opers.HTPredicate;

/**
 * Base class for interfacing LongOperator's with standard HTPredicate's
 * <p>
 * Implement a subclass that provides an implementation of  #getLong
 **/
public abstract class LogicalOperator2LongOperatorBridge<T> implements HTPredicate<T> {
    private LongOperator longOper;

    public LogicalOperator2LongOperatorBridge(LongOperator longOper) {
        this.longOper = longOper;
    }

    /**
     * Implement this method to provide the long value to compute the Long operator test against
     *
     * @param t
     * @return
     */
    public abstract long getLong(T t);

    @Override
    public void initForPass() {

    }

    @Override
    public boolean test(final T t) {
        return longOper.match(getLong(t));
    }

    @Override
    public boolean initFromMap(final JsonNode map) {
        return true;
    }

}

