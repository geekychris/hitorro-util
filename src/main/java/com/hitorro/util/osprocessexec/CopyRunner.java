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
package com.hitorro.util.osprocessexec;

import com.hitorro.util.io.IOUtil;

import java.io.*;

/**
 */
public class CopyRunner implements Runnable {
    private InputStream m_is = null;
    private java.io.OutputStream m_os = null;
    private String m_error = null;

    public void readFileIntoStream(java.io.OutputStream out, File inputFile)
            throws IOException {
        if (inputFile != null) {
            readStreamIntoStream(out, new FileInputStream(inputFile));
        }
    }

    public void writeToFileFromInputStream(InputStream in, File outputFile)
            throws IOException {
        if (outputFile != null) {
            writeStreamFromInputStream(in, new FileOutputStream(outputFile));
        }
    }

    public void readStreamIntoStream(java.io.OutputStream out, InputStream is) {
        m_is = is;
        m_os = out;
    }

    public void writeStreamFromInputStream(InputStream in, OutputStream os) {

        m_os = os;
        m_is = in;
    }


    public void run() {
        try {
            IOUtil.copyStream(m_is, m_os);
        } catch (IOException ioe) {
            m_error = ioe.getMessage();
        }
    }

    public String getError() {
        return m_error;
    }
}
