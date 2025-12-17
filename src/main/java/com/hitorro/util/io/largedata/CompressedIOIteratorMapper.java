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
package com.hitorro.util.io.largedata;

import com.fasterxml.jackson.databind.JsonNode;
import com.hitorro.util.basefile.fs.BaseFile;
import com.hitorro.util.core.Log;
import com.hitorro.util.core.iterator.AbstractIterator;
import com.hitorro.util.core.iterator.Mapper;

import java.io.IOException;

/**
 * Created by IntelliJ IDEA. User: Chris Date: 8/21/11 Time: 9:56 PM To change this template use File | Settings | File
 * Templates.
 */
public class CompressedIOIteratorMapper<T> implements Mapper<BaseFile, AbstractIterator<T>> {
    private BaseFileAccessingObjectFactory factory;

    public CompressedIOIteratorMapper(BaseFileAccessingObjectFactory factory) {
        this.factory = factory;
    }

    public String initPass(final JsonNode map) {
        return null;
    }

    public AbstractIterator<T> apply(final BaseFile bf) {
        try {
            return new CompressedStreamIOIterator(bf, factory);
        } catch (IOException e1) {
            Log.filesystem.error("Unable to open input file %s error %s %e", bf, e1, e1);
        }
        return null;
    }
}


