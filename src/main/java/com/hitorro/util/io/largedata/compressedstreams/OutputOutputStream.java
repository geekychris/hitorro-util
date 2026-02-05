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

import java.io.IOException;

public class OutputOutputStream extends COutputStream {
    java.io.DataOutputStream os;

    public OutputOutputStream(java.io.DataOutputStream os) throws IOException {
        this.os = os;
    }

    /**
     * output methods:
     */
    public final void flushBuffer(byte[] b, int size) throws IOException {
        os.write(b, 0, size);
    }

    public final void close() throws IOException {
        super.close();
        os.close();
    }

    /**
     * Random-access methods
     */
    public final OutputOutputStream seek(long pos) throws IOException {
        throw new IOException("Cannot seek with OutputOutputStream");
    }

    public final long length() throws IOException {
        // undefined
        return -1;
    }

    @SuppressWarnings("removal") // finalize() is deprecated for removal; consider using try-with-resources
    @Override
    protected final void finalize() throws IOException {
        // saw this weird case when the finalizer was getting a null pointer exception here
        // I suspect that if the constructor above fails, the VM still calls the finalizer
        // on the half-created object
        // I had my debugger set to break at NPEs and it stopped the finalizer thread
        // this is probably not needed, but safe
        if (os != null) {
            os.close();          // close the file
        }
    }

}

