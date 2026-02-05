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

/**
 * Lucene style Input Stream that wraps a java input stream.
 * <p/>
 * Note one should not use the seek method as seeking in an input stream doesnt make sense User: chris
 */
public class InputInputStream extends CInputStream {
    java.io.InputStream is;
    long isPosition = 0;
    boolean isClone;

    public InputInputStream(java.io.DataInputStream is, long expectedLength) throws IOException {
        this.is = is;
        length = expectedLength;
    }

    public void seek(long pos) throws IOException {
        throw new IOException("InputInputStream can not be seeked!");
    }

    /**
     * CInputStream methods
     */
    protected final void readInternal(byte[] b, int offset, int len) throws IOException {
        synchronized (is) {
            long position = getFilePointer();
            if (position != isPosition) {
                throw new IOException("position != isPosition");
            }
            int total = 0;
            do {
                int i = is.read(b, offset + total, len - total);
                if (i == -1) {
                    // now set length to the correct amount since we now know the end of the buffer
                    length = isPosition;
                }
                isPosition += i;
                total += i;
            }
            while (total < len);
        }
    }

    public final void close() throws IOException {
        if (!isClone) {
            is.close();
        }
    }

    /**
     * Random-access methods
     */
    protected final void seekInternal(long position) throws IOException {
    }

    @SuppressWarnings("removal") // finalize() is deprecated for removal; consider using try-with-resources
    @Override
    protected final void finalize() throws IOException {
        close();            // close the file
    }

    public Object clone() {
        FSInputStream clone = (FSInputStream) super.clone();
        clone.isClone = true;
        return clone;
    }
}
