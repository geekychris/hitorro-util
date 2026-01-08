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
package com.hitorro.util.io.largedata.iterator;

import com.hitorro.util.basefile.fs.BaseFile;
import com.hitorro.util.core.iterator.sinks.IteratorSink;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;

/**
 * <p/>
 * Given an iterator, put its data to some kind of output (for example HTSerializable to a file).
 */
public interface OutputIteratorSink<T> extends IteratorSink<T> {
    /**
     * If you wish to put to a file then provide a file name here...it will automatically close the file stream on
     * completion.
     *
     * @param file
     * @throws FileNotFoundException
     */
    void setFile(BaseFile file) throws IOException;

    /**
     * Set the output stream to write the content to.
     *
     * @param os
     * @param shouldCloseOnCompletion
     */
    void setOutput(OutputStream os, boolean shouldCloseOnCompletion);
}
