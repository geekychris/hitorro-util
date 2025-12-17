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
package com.hitorro.jsontypesystem.executors;

import com.hitorro.jsontypesystem.BaseT;
import com.hitorro.jsontypesystem.Type;
import com.hitorro.util.core.iterator.mappers.BaseMapper;
import com.hitorro.util.json.keys.propaccess.Propaccess;

import java.util.function.Predicate;

public abstract class BaseProjectionFactoryMapper<ACTION extends ExecutorAction> extends BaseMapper<Type, ExecutionBuilder> {
    protected Predicate<BaseT> predicate;

    @Override
    public ExecutionBuilder apply(final Type type) {
        Propaccess path = new Propaccess("");
        ExecutionBuilder<ACTION> mtv = getFactory();
        type.visit(mtv, predicate, path);
        mtv.finalizeNode();
        return mtv;
    }

    public Predicate<BaseT> getPredicate() {
        return predicate;
    }

    public void setPredicate(Predicate<BaseT> predicate) {
        this.predicate = predicate;
    }

    public abstract ExecutionBuilder<ACTION> getFactory();
}
