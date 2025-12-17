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
import java.io.OutputStream;

/**
 * allow a stream to goto two places.
 *
 * @author chris
 */
public class TeeOutputStream extends OutputStream {
    private OutputStream m_a;
    private OutputStream m_b;

    public TeeOutputStream(OutputStream a, OutputStream b) {
        m_a = a;
        m_b = b;
    }

    @Override
    public void write(int arg0) throws IOException {
        m_a.write(arg0);
        m_b.write(arg0);
    }

    public void close() throws IOException {
        m_a.close();
        m_b.close();
    }

    public void flush() throws IOException {
        m_a.flush();
        m_b.flush();

    }

    public void write(byte[] b) throws IOException {
        m_a.write(b);
        m_b.write(b);
    }

    public void write(byte[] b, int off, int len) throws IOException {
        m_a.write(b, off, len);
        m_b.write(b, off, len);
    }


}