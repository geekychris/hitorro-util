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
package com.hitorro.util.io.largedata.compressedstreams;

import com.hitorro.util.basefile.fs.BaseFile;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;


/**
 * Taken from Lucene and modified for a narrow compressed stream use case.
 */

public class FSOutputStream extends COutputStream {
    RandomAccessFile file = null;

    public FSOutputStream(BaseFile path) throws IOException {
        this(path.getLocalFileIfPossible());
    }

    public FSOutputStream(File path) throws IOException {
        file = new RandomAccessFile(path, "rw");
        file.setLength(0); // Truncate the file if it already exists
    }

    public FSOutputStream(File path, boolean append) throws IOException {
        file = new RandomAccessFile(path, "rw");
        if (append) {
            // seek to the end of the file (setting the fp correctly)
            this.seek(file.length());
        } else {
            file.setLength(0); // Truncate the file if it already exists
        }
    }

    /**
     * output methods:
     */
    public final void flushBuffer(byte[] b, int size) throws IOException {
        file.write(b, 0, size);
    }

    public final void close() throws IOException {
        super.close();
        file.close();
    }

    /**
     * Random-access methods
     */
    public final FSOutputStream seek(long pos) throws IOException {
        super.seek(pos);
        file.seek(pos);
        return this;
    }

    public final long length() throws IOException {
        return file.length();
    }

    @SuppressWarnings("removal") // finalize() is deprecated for removal; consider using try-with-resources
    @Override
    protected final void finalize() throws IOException {
        // saw this weird case when the finalizer was getting a null pointer exception here
        // I suspect that if the constructor above fails, the VM still calls the finalizer
        // on the half-created object
        // I had my debugger set to break at NPEs and it stopped the finalizer thread
        // this is probably not needed, but safe
        if (file != null) {
            file.close();          // close the file
        }
    }

}
