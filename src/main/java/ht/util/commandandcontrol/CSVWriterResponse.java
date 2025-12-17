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
package ht.util.commandandcontrol;

import ht.util.io.csv.CSVFormattedWriter;
import ht.util.io.csv.CSVWriter;

import java.util.List;

/*
 * Copyright (c) 2003 - present HiTorro All rights reserved.
 *
 * User: chris
 */
public class CSVWriterResponse implements CSVWriter {
    private CSVFormattedWriter writer;
    private Response response;

    public CSVWriterResponse(Response response, CSVFormattedWriter writer, String transaction, String rowName) {
        this.writer = writer;
        this.response = response;
        ResponseShape shape = new ResponseShape(transaction, rowName);
        shape.addHeader(writer.getColumnNames());
        response.setResponseShape(shape);
    }

    public void writeRow(Object values[]) {
        response.addRowArray(values);
    }

    public void writeRow(List<String> values) {
        response.addRowArray(values.toArray());
    }

    public void close() {
        response.end();
    }
}
