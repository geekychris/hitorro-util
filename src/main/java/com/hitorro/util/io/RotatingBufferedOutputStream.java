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

import java.io.OutputStream;

/**
 */
public class RotatingBufferedOutputStream extends OutputStream {

    private int m_size;
    private int m_bufferSize;
    private byte[] m_buffer;
    private int m_bytesWritten = 0;

    public RotatingBufferedOutputStream(int bufferSize) {
        m_size = bufferSize;
        m_bufferSize = bufferSize * 2;
        m_buffer = new byte[m_bufferSize];
    }

    public void write(int i) throws java.io.IOException {
        writeByte((byte) i);
    }

    private final void writeByte(byte b) {
        m_buffer[m_bytesWritten % m_bufferSize] = b;
        m_bytesWritten++;
    }

    public void write(byte[] bytes) throws java.io.IOException {
        write(bytes, 0, bytes.length);
    }

    /**
     * Come back and optimize with System.arrayCopy.....remember you have to deal with arrays being passed that are
     * multiples in size of our buffer and not blow up!
     *
     * @param bytes
     * @param i
     * @param i1
     * @throws java.io.IOException
     */
    public void write(byte[] bytes, int i, int i1) throws java.io.IOException {
        int end = i + i1;
        for (int j = i; j < end; j++) {
            writeByte(bytes[j]);
        }
    }

    public void flush() {

    }

    public void close() throws java.io.IOException {

    }

    public byte[] getWindow() {
        if (m_bytesWritten > m_bufferSize) {
            // we know we have a full window to return
            return getWindow(m_bytesWritten, m_size);
        } else {
            return getWindow(m_bytesWritten, m_bytesWritten);
        }
    }

    private byte[] getWindow(int currPosition, int size) {
        byte b[] = new byte[size];
        currPosition--;
        for (int i = 0; i < size; i++) {
            b[size - i - 1] = m_buffer[currPosition % m_bufferSize];
            currPosition--;
        }
        return b;
    }
}
