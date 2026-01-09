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

import com.hitorro.util.io.largedata.compressedstreams.CInputStream;

import java.io.IOException;

/**
 * Copyright (c) HiTorro 2003-2008, Inc.
 * <p/>
 * User: chris Date: May 11, 2004 Time: 10:52:22 AM
 * <p/>
 * Description:
 */
public class WordReader {

    private int BUFFER_SIZE = 1024;
    private int bytesRead;
    private boolean moreBytesToRead = true;
    private CInputStream m_stream;
    private char[] m_buff = new char[BUFFER_SIZE];

    public WordReader(CInputStream is) {
        m_stream = is;
    }

    private boolean fillBuffer()
            throws IOException {
        if (moreBytesToRead == false) {
            return false;
        }
        //bytesRead = m_stream.readChars(m_buff, 0, BUFFER_SIZE);
        if (bytesRead > 0) {
            return true;
        }
        return false;
    }
}
