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
 *
 */

/**
 * Abstract class for output to a file in a Directory.  A random-access output stream.  Used for all Lucene index output
 * operations.
 *
 * @see Directory
 * @see CInputStream
 */
public abstract class COutputStream {
    public static final int BUFFER_SIZE = 1024;

    private final byte[] buffer = new byte[BUFFER_SIZE];
    private long bufferStart = 0;              // position in file of buffer
    private int bufferPosition = 0;          // position in buffer

    public void writeDouble(double val) throws IOException {
        long j = Double.doubleToLongBits(val);
        writeByte((byte) (j >>> 0));
        writeByte((byte) (j >>> 8));
        writeByte((byte) (j >>> 16));
        writeByte((byte) (j >>> 24));
        writeByte((byte) (j >>> 32));
        writeByte((byte) (j >>> 40));
        writeByte((byte) (j >>> 48));
        writeByte((byte) (j >>> 56));
    }

    public void writeFloat(float val) throws IOException {
        int j = Float.floatToIntBits(val);
        writeByte((byte) (j >>> 0));
        writeByte((byte) (j >>> 8));
        writeByte((byte) (j >>> 16));
        writeByte((byte) (j >>> 24));
    }

    public final long getCurrentFileOffset() {
        return bufferStart + bufferPosition;
    }

    /**
     * Writes a single byte.
     *
     * @see CInputStream#readByte()
     */
    public final void writeByte(byte b) throws IOException {
        if (bufferPosition >= BUFFER_SIZE) {
            flush();
        }
        buffer[bufferPosition++] = b;
    }

    /**
     * Writes an array of bytes.
     *
     * @param b      the bytes to write
     * @param length the number of bytes to write
     *
     * @see CInputStream#readBytes(byte[], int, int)
     */
    public final void writeBytes(byte[] b, int length) throws IOException {
        for (int i = 0; i < length; i++) {
            writeByte(b[i]);
        }
    }

    /**
     * Writes an int as four bytes.
     *
     * @see CInputStream#readInt()
     */
    public final COutputStream writeShort(short i) throws IOException {
        writeByte((byte) (i >> 8));
        writeByte((byte) i);
        return this;
    }

    /**
     * Writes an int as four bytes.
     *
     * @see CInputStream#readInt()
     */
    public final COutputStream writeInt(int i) throws IOException {
        writeByte((byte) (i >> 24));
        writeByte((byte) (i >> 16));
        writeByte((byte) (i >> 8));
        writeByte((byte) i);
        return this;
    }

    /**
     * Writes an int in a variable-length format.  Writes between one and five bytes.  Smaller values take fewer bytes.
     * Negative numbers are not supported.
     *
     * @see CInputStream#readVInt()
     */
    public final COutputStream writeVInt(int i) throws IOException {
        while ((i & ~0x7F) != 0) {
            writeByte((byte) ((i & 0x7f) | 0x80));
            i >>>= 7;
        }
        writeByte((byte) i);
        return this;
    }

    /**
     * Writes a long as eight bytes.
     *
     * @see CInputStream#readLong()
     */
    public final COutputStream writeLong(long i) throws IOException {
        writeInt((int) (i >> 32));
        writeInt((int) i);
        return this;
    }

    /**
     * Writes an long in a variable-length format.  Writes between one and five bytes.  Smaller values take fewer bytes.
     * Negative numbers are not supported.
     *
     * @see CInputStream#readVLong()
     */
    public final COutputStream writeVLong(long i) throws IOException {
        while ((i & ~0x7F) != 0) {
            writeByte((byte) ((i & 0x7f) | 0x80));
            i >>>= 7;
        }
        writeByte((byte) i);
        return this;
    }

    /**
     * Writes a string.
     *
     * @see CInputStream#readString()
     */
    public final COutputStream writeString(String s) throws IOException {
        if (s == null) {
            writeVInt(0);
            return this;
        }
        int length = s.length();
        writeVInt(length);
        writeChars(s, 0, length);
        return this;
    }

    /**
     * Writes a sequence of UTF-8 encoded characters from a string.
     *
     * @param s      the source of the characters
     * @param start  the first character in the sequence
     * @param length the number of characters in the sequence
     *
     * @see CInputStream#readChars(char[], int, int)
     */
    public final COutputStream writeChars(String s, int start, int length)
            throws IOException {
        final int end = start + length;
        for (int i = start; i < end; i++) {
            final int code = (int) s.charAt(i);
            writeCharsAux(code);
        }
        return this;
    }

    private final void writeCharsAux(final int code) throws IOException {
        if (code >= 0x01 && code <= 0x7F) {
            writeByte((byte) code);
        } else if (((code >= 0x80) && (code <= 0x7FF)) || code == 0) {
            writeByte((byte) (0xC0 | (code >> 6)));
            writeByte((byte) (0x80 | (code & 0x3F)));
        } else {
            writeByte((byte) (0xE0 | (code >>> 12)));
            writeByte((byte) (0x80 | ((code >> 6) & 0x3F)));
            writeByte((byte) (0x80 | (code & 0x3F)));
        }
    }

    public final COutputStream writeChars(char buff[], int start, int length)
            throws IOException {
        final int end = start + length;
        for (int i = start; i < end; i++) {
            final int code = (int) buff[i];
            writeCharsAux(code);
        }
        return this;
    }

    /**
     * Forces any buffered output to be written.
     */
    public final void flush() throws IOException {
        flushBuffer(buffer, bufferPosition);
        bufferStart += bufferPosition;
        bufferPosition = 0;
    }

    /**
     * Expert: implements buffer write.  Writes bytes at the current position in the output.
     *
     * @param b   the bytes to write
     * @param len the number of bytes to write
     */
    protected abstract void flushBuffer(byte[] b, int len) throws IOException;

    /**
     * Closes this stream to further operations.
     */
    public void close() throws IOException {
        flush();
    }

    /**
     * Returns the current position in this file, where the next write will occur.
     *
     * @see #seek(long)
     */
    public final long getFilePointer() throws IOException {
        return bufferStart + bufferPosition;
    }

    /**
     * Sets current position in this file, where the next write will occur.
     *
     * @see #getFilePointer()
     */
    public COutputStream seek(long pos) throws IOException {
        flush();
        bufferStart = pos;
        return this;
    }

    /**
     * The number of bytes in the file.
     */
    public abstract long length() throws IOException;


}

