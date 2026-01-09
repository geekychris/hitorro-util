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
package com.hitorro.util.core.iterator;

import java.io.IOException;
import java.io.LineNumberReader;
import java.io.Reader;

public class LineReaderIterator extends AbstractIterator<String> {
    private String currentRow = null;

    private boolean m_open = true;

    private LineNumberReader m_reader;

    public LineReaderIterator(Reader reader) {
        m_reader = new LineNumberReader(reader);
    }

    public boolean hasNext() {
        return readAux();
    }

    public String next() {
        readAux();
        String returnThis = currentRow;
        currentRow = null;
        return returnThis;
    }

    private boolean readAux() {
        if (!m_open) {
            // closed
            return false;
        }
        if (currentRow == null) {
            try {
                currentRow = m_reader.readLine();
                if (currentRow == null) {
                    m_open = false;
                    m_reader.close();
                    return false;
                }
            } catch (IOException ioe) {
                m_open = false;
                return false;
            }
        }
        return true;
    }

    public void remove() {
        // Not implemented
        assert false;
    }

    @Override
    public void close() throws Exception {
        m_reader.close();
    }
}
