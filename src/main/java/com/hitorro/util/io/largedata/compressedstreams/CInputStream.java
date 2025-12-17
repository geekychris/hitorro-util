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

/**
 * Copyright 2004 The Apache Software Foundation
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.IOException;

/**
 * Abstract base class for input from a file in a {@link Directory}.  A random-access input stream.  Used for all Lucene
 * index input operations.
 *
 * @see Directory
 * @see COutputStream
 */
public abstract class CInputStream implements Cloneable {
    public static final int BUFFER_SIZE = COutputStream.BUFFER_SIZE;

    protected byte[] buffer;
    protected int bufferSize = BUFFER_SIZE;
    protected boolean readOnlyBuffer = false;
    protected long length;              // set by subclasses
    protected boolean throwExceptionOnEof = true;
    private char[] chars;
    private long bufferStart = 0;              // position in file of buffer
    private int bufferLength = 0;              // end of valid bytes
    private int bufferPosition = 0;          // next byte to read

    public double readDouble()
            throws IOException {
        long j = ((readByte() & 0xFFL) << 0) +
                ((readByte() & 0xFFL) << 8) +
                ((readByte() & 0xFFL) << 16) +
                ((readByte() & 0xFFL) << 24) +
                ((readByte() & 0xFFL) << 32) +
                ((readByte() & 0xFFL) << 40) +
                ((readByte() & 0xFFL) << 48) +
                ((readByte() & 0xFFL) << 56);
        return Double.longBitsToDouble(j);
    }

    public float readFloat()
            throws IOException {
        int j = ((readByte() & 0xFF) << 0) +
                ((readByte() & 0xFF) << 8) +
                ((readByte() & 0xFF) << 16) +
                ((readByte() & 0xFF) << 24);
        return Float.intBitsToFloat(j);
    }

    /**
     * Reads and returns a single byte.
     *
     * @see COutputStream#writeByte(byte)
     */
    public final byte readByte() throws IOException {
        if (bufferPosition >= bufferLength) {
            refill();
        }
        return buffer[bufferPosition++];
    }

    /**
     * array we have at hand.
     *
     * @param bufferIn
     */
    public void setBuffer(byte[] bufferIn) {
        readOnlyBuffer = true;
        buffer = bufferIn;
        bufferSize = bufferIn.length;
        bufferStart = 0;
        bufferLength = bufferIn.length;
        bufferPosition = 0;
    }

    /**
     * Reads a specified number of bytes into an array at the specified offset.
     *
     * @param b      the array to read bytes into
     * @param offset the offset in the array to start storing bytes
     * @param len    the number of bytes to read
     *
     * @see COutputStream#writeBytes(byte[], int)
     */
    public final void readBytes(byte[] b, int offset, int len)
            throws IOException {
        if (len < bufferSize) {
            for (int i = 0; i < len; i++)          // read byte-by-byte
            {
                b[i + offset] = readByte();
            }
        } else {                      // read all-at-once
            long start = getFilePointer();
            seekInternal(start);
            readInternal(b, offset, len);

            bufferStart = start + len;          // adjust stream variables
            bufferPosition = 0;
            bufferLength = 0;                  // trigger refill() on read
        }
    }

    /**
     * Reads four bytes and returns an int.
     *
     * @see COutputStream#writeInt(int)
     */
    public final int readInt() throws IOException {
        return ((readByte() & 0xFF) << 24) | ((readByte() & 0xFF) << 16)
                | ((readByte() & 0xFF) << 8) | (readByte() & 0xFF);
    }

    /**
     * Reads four bytes and returns an int.
     *
     * @see COutputStream#writeInt(int)
     */
    public short readShort() throws IOException {
        int result = ((readByte() & 0xFF) << 8) | (readByte() & 0xFF);
        return (short) result;
    }

    /**
     * Reads an int stored in variable-length format.  Reads between one and five bytes.  Smaller values take fewer
     * bytes.  Negative numbers are not supported.
     *
     * @see COutputStream#writeVInt(int)
     */
    public final int readVInt() throws IOException {
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
    public final long readLong() throws IOException {
        return (((long) readInt()) << 32) | (readInt() & 0xFFFFFFFFL);
    }

    /**
     * Reads a long stored in variable-length format.  Reads between one and nine bytes.  Smaller values take fewer
     * bytes.  Negative numbers are not supported.
     */
    public final long readVLong() throws IOException {
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
     * @see COutputStream#writeString(String)
     */
    public final String readString() throws IOException {
        int length = readVInt();
        if (length == 0) {
            return null;
        }
        if (chars == null || length > chars.length) {
            chars = new char[length];
        }
        readChars(chars, 0, length);
        return new String(chars, 0, length);
    }

    /**
     * Reads UTF-8 encoded characters into an array.
     *
     * @param buffer the array to read characters into
     * @param start  the offset in the array to start storing characters
     * @param length the number of characters to read
     *
     * @see COutputStream#writeChars(String, int, int)
     */
    public final void readChars(char[] buffer, int start, int length)
            throws IOException {
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


    private void refill() throws IOException {
        long start = bufferStart + bufferPosition;
        long end = start + BUFFER_SIZE;
        if (length != -1 && end > length)                  // don't read past EOF
        {
            end = length;
        }
        bufferLength = (int) (end - start);
        if (bufferLength == 0) {
            throw new IOException("read past EOF");
        }

        if (buffer == null) {
            buffer = new byte[bufferSize];          // allocate buffer lazily
        }
        readInternal(buffer, 0, bufferLength);

        bufferStart = start;
        bufferPosition = 0;
    }

    /**
     * Expert: implements buffer refill.  Reads bytes from the current position in the input.
     *
     * @param b      the array to read bytes into
     * @param offset the offset in the array to start storing bytes
     * @param length the number of bytes to read
     */
    protected abstract void readInternal(byte[] b, int offset, int length)
            throws IOException;

    /**
     * Closes the stream to futher operations.
     */
    public abstract void close() throws IOException;

    /**
     * Returns the current position in this file, where the next read will occur.
     *
     * @see #seek(long)
     */
    public final long getFilePointer() {
        return bufferStart + bufferPosition;
    }

    /**
     * Sets current position in this file, where the next read will occur.
     *
     * @see #getFilePointer()
     */
    public void seek(long pos) throws IOException {
        if (pos >= bufferStart && pos < (bufferStart + bufferLength)) {
            bufferPosition = (int) (pos - bufferStart);  // seek within buffer
        } else {
            bufferStart = pos;
            bufferPosition = 0;
            bufferLength = 0;                  // trigger refill() on read()
            seekInternal(pos);
        }
    }

    /**
     * Expert: implements seek.  Sets current position in this file, where the next {@link #readInternal(byte[], int,
     * int)} will occur.
     *
     * @see #readInternal(byte[], int, int)
     */
    protected abstract void seekInternal(long pos) throws IOException;

    /**
     * The number of bytes in the file.
     */
    public final long length() {
        return length;
    }

    /**
     * Returns a clone of this stream.
     * <p/>
     * <p>Clones of a stream access the same data, and are positioned at the same point as the stream they were cloned
     * from.
     * <p/>
     * <p>Expert: Subclasses must ensure that clones may be positioned at different points in the input from each other
     * and from the stream they were cloned from.
     */
    public Object clone() {
        CInputStream clone = null;
        try {
            clone = (CInputStream) super.clone();
        } catch (CloneNotSupportedException e) {
        }

        if (buffer != null) {
            clone.buffer = new byte[bufferSize];
            System.arraycopy(buffer, 0, clone.buffer, 0, bufferLength);
        }

        clone.chars = null;

        return clone;
    }

}
