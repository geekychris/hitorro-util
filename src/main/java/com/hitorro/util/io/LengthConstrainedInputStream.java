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
package com.hitorro.util.io;

import java.io.IOException;
import java.io.InputStream;

/**
 * <p/>
 * Wrapper around an input stream to limit the amount of bytes that can be read.  This is usefull for streams within
 * streams.
 */
public class LengthConstrainedInputStream extends InputStream {
    private long bytesToRead;
    private InputStream m_is;

    public LengthConstrainedInputStream(InputStream is, long bytesToRead) {
        m_is = is;
        this.bytesToRead = bytesToRead;
    }

    public int read() throws IOException {
        return m_is.read();
    }


    public int read(byte b[]) throws IOException {
        return read(b, 0, b.length);
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
        int read = 0;
        if (bytesToRead < len) {
            len = (int) bytesToRead;
        }
        if (bytesToRead == 0) {
            return -1;
        }
        read = m_is.read(b, off, len);
        if (read == -1) {
            return -1;
        }
        bytesToRead -= read;
        return read;
    }

    public long skip(long n) throws IOException {
        if (bytesToRead < n) {
            n = (int) bytesToRead;
        }
        bytesToRead = 0;
        return m_is.skip(n);
    }


    public int available() {
        return 0;
    }

    public void close() throws IOException {
        // do nothing
    }

    public synchronized void mark(int readlimit) {
        m_is.mark(readlimit);
    }

    public synchronized void reset() throws IOException {
        throw new IOException("mark/reset not supported");
    }


    public boolean markSupported() {
        return m_is.markSupported();
    }

}
