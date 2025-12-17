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
package com.hitorro.util.basefile.fs.sinks;


import com.fasterxml.jackson.databind.JsonNode;
import gnu.trove.map.hash.TObjectIntHashMap;
import com.hitorro.util.basefile.fs.BaseFile;
import com.hitorro.util.io.StoreException;

import java.io.IOException;

/**
 *
 */
public abstract class HashCountingBaseFileStateSink<E> extends BaseFileStatsSink<E> {
    protected TObjectIntHashMap<E> set = new TObjectIntHashMap();

    public HashCountingBaseFileStateSink(BaseFile outputFile) {
        super(outputFile);
    }

    @Override
    public boolean init(JsonNode node) {
        return true;
    }

    @Override
    public boolean start() {
        set.clear();
        return true;
    }

    @Override
    public boolean add(final E o) throws IOException, StoreException {
        if (o == null) {
            // dont store nulls
            return true;
        }
        if (set.contains(o)) {
            set.increment(o);
        } else {
            set.put(o, 1);
        }
        return true;
    }
}
