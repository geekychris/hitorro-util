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
package com.hitorro.util.basefile.fs.jarfile;

import java.io.IOException;
import java.io.InputStream;

/**
 *
 */
public class ZipInputWrapper extends InputStream {
    private InputStream m_is;

    public ZipInputWrapper(InputStream is) {
        m_is = is;
    }

    public int read() throws IOException {
        return m_is.read();
    }

    public int read(byte b[]) throws IOException {
        return m_is.read(b, 0, b.length);
    }

    /**
     * read limited to the amount of bytes we requested.
     *
     * @param b
     * @param off
     * @param len
     * @return
     * @throws IOException
     */
    public int read(byte b[], int off, int len) throws IOException {
        return m_is.read(b, off, len);
    }

    public long skip(long n) throws IOException {
        return m_is.skip(n);
    }


    public int available() throws IOException {
        return m_is.available();
    }

    public void close() throws IOException {
        // do nothing
        // we dont want the underlying object to have close called on it
    }

    public synchronized void mark(int readlimit) {
        m_is.mark(readlimit);
    }

    public synchronized void reset() throws IOException {
        m_is.reset();
    }

    public boolean markSupported() {
        return m_is.markSupported();
    }
}
