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
package com.hitorro.util.commandandcontrol;

import com.hitorro.util.io.csv.CSVFileWriter;

import java.io.File;

/*
 *
 * User: chris
 */
public class CSVResponse extends Response {
    CSVFileWriter m_File_writer = null;
    private File m_file = null;

    public CSVResponse(File f) {
        m_file = f;
    }

    public void setResponseShape(ResponseShape shape) {
        super.setResponseShape(shape);
        addHeaderArray(shape.m_header.getHeader());
    }

    public void addBannerRow(String row) {

    }

    public void addHeader(String... columnHeaders) {
        addHeaderArray(columnHeaders);
    }

    @Override
    public void addStatusUpdateMessage(final String info, final int percentComplete) {
        // do nothing
    }

    public void addHeaderArray(String columnHeaders[]) {
        m_File_writer = new CSVFileWriter(m_file, columnHeaders);
    }

    public void addRow(Object... elements) {
        addRowArray(elements);
    }

    public void addRowArray(Object elements[]) {
        m_File_writer.writeRow(elements);
    }


    public void addInfo(InfoLevel level, String info) {
    }

    public void end() {
        m_File_writer.close();
    }
}
