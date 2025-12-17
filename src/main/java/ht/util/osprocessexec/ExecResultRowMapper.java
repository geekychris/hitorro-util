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
package ht.util.osprocessexec;

import ht.util.core.Constants;
import ht.util.core.iterator.AbstractIterator;
import ht.util.core.iterator.LineReaderIterator;

import java.io.*;
import java.util.List;

/**
 * Copyright (c) 2003 - present HiTorro All rights reserved.
 * <p/>
 * User: chris
 */
public class ExecResultRowMapper implements TerminationKey {
    private String m_program;
    private String[] m_args;

    public ExecResultRowMapper(String program, String args[]) {
        m_program = program;
        m_args = args;
    }

    private static void getLine(byte[] array, int start, int end, int minspaces, List<String> list) {
        int s = start;
        int curr = start;
        int spaceIndexStart = -1;
        while (curr < end) {
            if (array[curr] != ' ') {
                spaceIndexStart = -1;
                curr++;
            } else if (spaceIndexStart >= curr - minspaces) {
                // we have hit at least minspaces, this is a column
                list.add(new String(array, s, spaceIndexStart - 1));

                while (curr < end && array[curr] == ' ') {
                    // step over the spaces
                    curr++;
                }
                s = curr;
            } else {
                if (spaceIndexStart == -1) {
                    spaceIndexStart = curr;
                }
                curr++;
            }
        }
    }

    private static int indexOf(byte[] array, int startIndex, char c) {
        while (startIndex < array.length) {
            if (array[startIndex] == c) {
                return startIndex;
            }
            startIndex++;
        }
        return -1;
    }

    public AbstractIterator<String> map(int maxWaitSeconds)
            throws IOException, InterruptedException {
        ExecContext ec = new ExecContext(this);
        ec.setProgram(m_program);
        ec.setArgs(m_args);
        ByteArrayOutputStream os = new ByteArrayOutputStream();

        ec.setOutput(os);
        int resultCode = ec.execAndWaitFromProcessBuilder((long) (Constants.MillisInSecond * maxWaitSeconds));

        Reader reader = new InputStreamReader(new ByteArrayInputStream(os.toByteArray()));
        LineReaderIterator iter = new LineReaderIterator(reader);

        return iter;
    }

    public void complete(int exitCode) {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
