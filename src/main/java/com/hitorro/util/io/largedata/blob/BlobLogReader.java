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
package com.hitorro.util.io.largedata.blob;

import com.hitorro.util.core.Log;

import java.io.DataInputStream;
import java.io.IOException;

/**
 */
public class BlobLogReader {
    private DataInputStream dis;

    public BlobLogReader(DataInputStream dis) {
        this.dis = dis;
    }

    /**
     * read into a byte array the next blob.  If a buffer is provided AND the the blob will fit, then the provided
     * buffer will be used.  If not, a new array will be used.
     *
     * @return
     * @throws java.io.EOFException if this input stream reaches the end before reading eight bytes.
     * @throws java.io.IOException  if an I/O error occurs.
     */
    public byte[] read(byte[] buffer)
            throws IOException {
        long length = this.dis.readLong();
        if (buffer == null || buffer.length < length) {
            // not big enough, grow
            buffer = new byte[(int) length];
        }
        int lengthRead = this.dis.read(buffer, 0, (int) length);
        if (lengthRead != length) {
            Log.util.error("BlobLogReader.read could not read blob, expected length %s got %s",
                    length, lengthRead);
        }
        return buffer;
    }
}
