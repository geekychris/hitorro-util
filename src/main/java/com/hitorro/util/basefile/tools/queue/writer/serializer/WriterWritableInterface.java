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
package com.hitorro.util.basefile.tools.queue.writer.serializer;

import com.hitorro.util.basefile.fs.BaseFile;
import com.hitorro.util.io.StoreException;

import java.io.IOException;


/**
 * Responsible for providing a way to convertToPdf an object into a series of bytes using whatever serialization
 * required. Ontop of that it assumes that not all file formats can just be read without a terminator (the readers may
 * require some kind of end of file marker.  For that reason there is a closeout function that can be implemented to
 * take a file and append the appropriate marker before being copied out of the writers disk cache.
 */
public interface WriterWritableInterface<T> {
    void applyCloseToFile(BaseFile f) throws IOException;

    int getBytes(T t) throws IOException;

    String getExtension();

    void setExtension(String ext);

    boolean open(BaseFile f) throws IOException;

    boolean close() throws IOException;

    boolean write(T sd) throws IOException, StoreException;
}
