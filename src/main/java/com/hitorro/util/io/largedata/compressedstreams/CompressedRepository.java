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

import com.hitorro.util.core.ArrayUtil;
import com.hitorro.util.core.HTAssert;

import java.io.File;
import java.io.IOException;

/**
 * Copyright (c) HiTorro 2003-2008, Inc.
 * <p/>
 * User: chris Date: Apr 22, 2004 Time: 10:11:59 PM
 * <p/>
 * Description:
 */
public class CompressedRepository {
    static final int BUFFER_SIZE = COutputStream.BUFFER_SIZE;

    protected byte[] m_buffer;
    protected int m_bufferPosition = 0;          // next byte to read
    protected long m_length;              // set by subclasses
    private char[] m_chars;
    private long m_bufferStart = 0;              // position in file of buffer
    private int m_bufferLength = 0;              // end of valid bytes


    public CompressedRepository(byte[] buff) {
        m_buffer = buff;
        m_bufferLength = m_buffer.length;
    }

    public static final CompressedRepository getCompressedRepository(File file) throws IOException {
        CompressedRepository repo =
                new CompressedRepository(ArrayUtil.loadByteArrayFromFile(file));

        return repo;
    }

    public CompressedRepository getCopy() {
        return new CompressedRepository(m_buffer);
    }

    /**
     * Reads and returns a single byte.
     *
     * @see COutputStream#writeByte(byte)
     */
    public final byte readByte() {
        if (m_bufferPosition >= m_bufferLength) {
            HTAssert.assertThat(false, "should not read past end of buffer");
        }
        return m_buffer[m_bufferPosition++];
    }

    public void seek(int index) {
        m_bufferPosition = index;
    }

    /**
     * Reads four bytes and returns an int.
     *
     * @see COutputStream#writeInt(int)
     */
    public final int readInt() {
        return ((readByte() & 0xFF) << 24) | ((readByte() & 0xFF) << 16)
                | ((readByte() & 0xFF) << 8) | (readByte() & 0xFF);
    }

    /**
     * Reads an int stored in variable-length format.  Reads between one and five bytes.  Smaller values take fewer
     * bytes.  Negative numbers are not supported.
     *
     * @see COutputStream#writeVInt(int)
     */
    public final int readVInt() {
        byte b = readByte();
        int i = b & 0x7F;
        for (int shift = 7; (b & 0x80) != 0; shift += 7) {
            b = readByte();
            i |= (b & 0x7F) << shift;
        }
        return i;
    }

    /**
     * Reads eight bytes and returns a long.
     *
     * @see COutputStream#writeLong(long)
     */
    public final long readLong() {
        return (((long) readInt()) << 32) | (readInt() & 0xFFFFFFFFL);
    }

    /**
     * Reads a long stored in variable-length format.  Reads between one and nine bytes.  Smaller values take fewer
     * bytes.  Negative numbers are not supported.
     */
    public final long readVLong() {
        byte b = readByte();
        long i = b & 0x7F;
        for (int shift = 7; (b & 0x80) != 0; shift += 7) {
            b = readByte();
            i |= (b & 0x7FL) << shift;
        }
        return i;
    }

    /**
     * Reads a string.
     *
     * @see COutputStream#writeString(java.lang.String)
     */
    public final String readString() {
        int length = readVInt();
        if (m_chars == null || length > m_chars.length) {
            m_chars = new char[length];
        }
        readChars(m_chars, 0, length);
        return new String(m_chars, 0, length);
    }

    /**
     * Reads UTF-8 encoded characters into an array.
     *
     * @param buffer the array to read characters into
     * @param start  the offset in the array to start storing characters
     * @param length the number of characters to read
     * @see COutputStream#writeChars(java.lang.String, int, int)
     */
    public final void readChars(char[] buffer, int start, int length) {
        final int end = start + length;
        for (int i = start; i < end; i++) {
            byte b = readByte();
            if ((b & 0x80) == 0) {
                buffer[i] = (char) (b & 0x7F);
            } else if ((b & 0xE0) != 0xE0) {
                buffer[i] = (char) (((b & 0x1F) << 6)
                        | (readByte() & 0x3F));
            } else {
                buffer[i] = (char) (((b & 0x0F) << 12)
                        | ((readByte() & 0x3F) << 6)
                        | (readByte() & 0x3F));
            }
        }
    }


    /**
     * Returns the current position in this file, where the next read will occur.
     */
    public final long getFilePointer() {
        return m_bufferStart + m_bufferPosition;
    }


    /**
     * The number of bytes in the file.
     */
    public final long length() {
        return m_length;
    }


}
