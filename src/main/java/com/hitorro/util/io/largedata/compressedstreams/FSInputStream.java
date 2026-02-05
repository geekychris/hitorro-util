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

import com.hitorro.util.basefile.fs.BaseFile;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * XXX TODO CJC This code is taken from lucene 1.4.3 and is temporary we should consider license usage and or writing
 * our own version of these routines.
 */

public final class FSInputStream extends CInputStream {
    Descriptor file = null;
    boolean isClone;

    public FSInputStream(File path) throws IOException {
        file = new Descriptor(path, "r");
        length = file.length();
    }

    public FSInputStream(BaseFile path) throws IOException {
        file = new Descriptor(path.getLocalFileIfPossible(), "r");
        length = file.length();
    }

    /**
     * CInputStream methods
     */
    protected final void readInternal(byte[] b, int offset, int len)
            throws IOException {
        synchronized (file) {
            long position = getFilePointer();
            if (position != file.position) {
                file.seek(position);
                file.position = position;
            }
            int total = 0;
            do {
                int i = file.read(b, offset + total, len - total);
                if (i == -1) {
                    throw new IOException("read past EOF");
                }
                file.position += i;
                total += i;
            }
            while (total < len);
        }
    }

    public final void close() throws IOException {
        if (!isClone) {
            file.close();
        }
    }

    /**
     * Random-access methods
     */
    protected final void seekInternal(long position) {
    }

    @SuppressWarnings("removal") // finalize() is deprecated for removal; consider using try-with-resources
    @Override
    protected final void finalize() throws IOException {
        close();            // close the file
    }

    public Object clone() {
        FSInputStream clone = (FSInputStream) super.clone();
        clone.isClone = true;
        return clone;
    }

    /**
     * Method used for testing. Returns true if the underlying file descriptor is valid.
     */
    boolean isFDValid() throws IOException {
        return file.getFD().valid();
    }

    private class Descriptor extends RandomAccessFile {
        /* DEBUG */
        //private String name;
        /* DEBUG */
        public long position;

        public Descriptor(File file, String mode) throws IOException {
            super(file, mode);
            /* DEBUG */
            //name = file.toString();
            //debug_printInfo("OPEN");
            /* DEBUG */
        }

    }
}