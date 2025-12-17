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

import com.hitorro.util.io.FileUtil;

import java.io.File;
import java.io.IOException;

/**
 * Utility functions for accessing compressed streams of data
 *
 * @author chris
 */
public class CompressedStreamUtil {

    /**
     * Create an compressed input stream either disk or ram based.
     *
     * @param file
     * @param ramBased
     * @return input stream that is ram based (require no further io) or a disk based input stream that uses a 1k
     * buffer.
     * @throws IOException
     */
    public static final CInputStream getInputStream(File file, boolean ramBased)
            throws IOException {
        if (ramBased) {
            return getInputStreamRAM(file);
        } else {
            return getInputStreamDisk(file);
        }
    }

    public static final CInputStream getInputStreamFromByteArray(byte buff[]) {
        RAMInputStream is = new RAMInputStream(null);
        is.setBuffer(buff);
        return is;
    }

    /**
     * Ram based input stream.  reads the whole file into memory and wraps it in a ram input stream.
     *
     * @param file
     * @return
     * @throws IOException
     */
    public static final CInputStream getInputStreamRAM(File file)
            throws IOException {
        byte buffer[] = FileUtil.getFileAsByteArray(file);
        RAMInputStream is = new RAMInputStream(null);
        is.setBuffer(buffer);
        return is;
    }

    /**
     * Disk based input stream.  All reads go via a 1k buffer but ultimately require disk io's...though the os may be
     * good to you :-}
     *
     * @param file
     * @return
     * @throws IOException
     */
    public static final CInputStream getInputStreamDisk(File file)
            throws IOException {
        FSInputStream is = new FSInputStream(file);
        return is;
    }

}
