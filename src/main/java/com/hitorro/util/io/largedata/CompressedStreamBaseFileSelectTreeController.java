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

import com.hitorro.util.basefile.fs.BaseFile;
import com.hitorro.util.core.iterator.AbstractIterator;
import com.hitorro.util.io.largedata.iterator.BaseFileSelectTreeController;

import java.io.IOException;

/**
 *
 */
public class CompressedStreamBaseFileSelectTreeController<T extends CompressedStreamIO> extends BaseFileSelectTreeController {
    protected BaseFileAccessingObjectFactory<T> factory;

    public CompressedStreamBaseFileSelectTreeController(final BaseFile dir, final BaseFile[] fList, final int maxPerIter, final boolean deleteOnceMerged,
                                                        final String finalFileExtension, BaseFileAccessingObjectFactory<T> fac, boolean removeOriginalFiles) throws IOException {
        super(dir, fList, maxPerIter, fac, deleteOnceMerged, finalFileExtension, removeOriginalFiles);
        factory = fac;
    }

    protected AbstractIterator<T> getIterator(BaseFile fc) throws IOException {
        return new CompressedStreamIOIterator(fc, factory);
    }
}
