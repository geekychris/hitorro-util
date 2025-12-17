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
 * Basic CInputStream deligation class that allows you to overide any of the methods you wish to intercept
 */
public abstract class InputStreamDeligate extends InputStream {
    private final InputStream m_inputStream;

    public InputStreamDeligate(InputStream inputStream) {
        m_inputStream = inputStream;
    }

    @Override
    public int read() throws IOException {
        return m_inputStream.read();
    }


    @Override
    public int read(byte[] bytes) throws IOException {
        return m_inputStream.read(bytes);
    }

    @Override
    public int read(byte[] bytes, int i, int i1) throws IOException {
        return m_inputStream.read(bytes, i, i1);
    }

    @Override
    public long skip(long l) throws IOException {
        return m_inputStream.skip(l);
    }

    @Override
    public int available() throws IOException {
        return m_inputStream.available();
    }

    @Override
    public void close() throws IOException {
        m_inputStream.close();
    }

    @Override
    public void mark(int i) {
        m_inputStream.mark(i);
    }

    @Override
    public void reset() throws IOException {
        m_inputStream.reset();
    }

    @Override
    public boolean markSupported() {
        return m_inputStream.markSupported();
    }
}
