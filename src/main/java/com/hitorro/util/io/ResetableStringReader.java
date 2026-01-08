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
import java.io.Reader;

/**
 * <p/>
 * Adaptation of the StringReader.
 */
public class ResetableStringReader extends Reader {

    private String str;
    private int length;
    private int next = 0;
    private int mark = 0;

    /**
     * Create a new string reader.
     *
     * @param s String providing the character stream.
     */
    public ResetableStringReader(String s) {
        set(s);
    }

    public void set(String s) {
        this.str = s;
        if (s != null) {
            this.length = s.length();
        }
        mark = 0;
        next = 0;
    }

    /**
     * Check to make sure that the stream has not been closed
     */
    private void ensureOpen() throws IOException {
        if (str == null) {
            throw new IOException("Stream closed");
        }
    }

    /**
     * Read a single character.
     *
     * @return The character read, or -1 if the end of the stream has been reached
     * @throws IOException If an I/O error occurs
     */
    public int read() throws IOException {
        synchronized (lock) {
            ensureOpen();
            if (next >= length) {
                return -1;
            }
            return str.charAt(next++);
        }
    }

    /**
     * Read characters into a portion of an array.
     *
     * @param cbuf Destination buffer
     * @param off  Offset at which to start writing characters
     * @param len  Maximum number of characters to read
     * @return The number of characters read, or -1 if the end of the stream has been reached
     * @throws IOException If an I/O error occurs
     */
    public int read(char cbuf[], int off, int len) throws IOException {
        synchronized (lock) {
            ensureOpen();
            if ((off < 0) || (off > cbuf.length) || (len < 0) ||
                    ((off + len) > cbuf.length) || ((off + len) < 0)) {
                throw new IndexOutOfBoundsException();
            } else if (len == 0) {
                return 0;
            }
            if (next >= length) {
                return -1;
            }
            int n = Math.min(length - next, len);
            str.getChars(next, next + n, cbuf, off);
            next += n;
            return n;
        }
    }

    /**
     * Skips the specified number of characters in the stream. Returns the number of characters that were skipped.
     * <p/>
     * <p>The <code>ns</code> parameter may be negative, even though the <code>skip</code> method of the {@link Reader}
     * superclass throws an exception in this case. Negative values of <code>ns</code> cause the stream to skip
     * backwards. Negative return values indicate a skip backwards. It is not possible to skip backwards past the
     * beginning of the string.
     * <p/>
     * <p>If the entire string has been read or skipped, then this method has no effect and always returns 0.
     *
     * @throws IOException If an I/O error occurs
     */
    public long skip(long ns) throws IOException {
        synchronized (lock) {
            ensureOpen();
            if (next >= length) {
                return 0;
            }
            // Bound skip by beginning and end of the source
            long n = Math.min(length - next, ns);
            n = Math.max(-next, n);
            next += n;
            return n;
        }
    }

    /**
     * Tell whether this stream is ready to be read.
     *
     * @return True if the next read() is guaranteed not to block for input
     * @throws IOException If the stream is closed
     */
    public boolean ready() throws IOException {
        synchronized (lock) {
            ensureOpen();
            return true;
        }
    }

    /**
     * Tell whether this stream supports the mark() operation, which it does.
     */
    public boolean markSupported() {
        return true;
    }

    /**
     * Mark the present position in the stream.  Subsequent calls to reset() will reposition the stream to this point.
     *
     * @param readAheadLimit Limit on the number of characters that may be read while still preserving the mark. Because
     *                       the stream's input comes from a string, there is no actual limit, so this argument must not
     *                       be negative, but is otherwise ignored.
     * @throws IllegalArgumentException If readAheadLimit is < 0
     * @throws IOException              If an I/O error occurs
     */
    public void mark(int readAheadLimit) throws IOException {
        if (readAheadLimit < 0) {
            throw new IllegalArgumentException("Read-ahead limit < 0");
        }
        synchronized (lock) {
            ensureOpen();
            mark = next;
        }
    }

    /**
     * Reset the stream to the most recent mark, or to the beginning of the string if it has never been marked.
     *
     * @throws IOException If an I/O error occurs
     */
    public void reset() throws IOException {
        synchronized (lock) {
            ensureOpen();
            next = mark;
        }
    }

    /**
     * Close the stream.
     */
    public void close() {
        str = null;
    }

}

