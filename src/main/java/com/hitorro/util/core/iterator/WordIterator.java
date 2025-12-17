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

import com.hitorro.util.core.Log;

/**
 * Copyright (c) HiTorro 2003-2008, Inc.
 * <p/>
 * User: chris Date: Apr 19, 2004 Time: 3:49:08 PM
 * <p/>
 * Description:
 */
public class WordIterator {
    private static final char Space = ' ';
    private String m_string;
    private int m_length;
    private int m_start = 0;
    private int m_end = 0;
    private boolean m_hasWord = false;

    public int getStart() {
        return m_start;
    }

    public int getEnd() {
        return m_end;
    }

    public void setString(String string) {
        m_string = string;
        m_length = m_string.length();
        m_hasWord = reset();
    }

    public boolean hasWord() {
        return m_hasWord;
    }

    public boolean nextWord() {
        m_hasWord = nextWord(m_start + 1);
        return m_hasWord;
    }

    public String getWord() {
        if (m_start == 0 && m_end == m_length - 1) {
            // its the same string as we came in with
            return m_string;
        }
        return m_string.substring(m_start, m_end);
    }

    public boolean reset() {
        return nextWord(0);
    }

    private boolean nextWord(int i) {
        if (i != 0) {
            m_start = getFirstSpace(i);
            if (m_start == -1) {
                return false;
            }
            m_start = getFirstNonSpace(m_start);
        } else {
            m_start = getFirstNonSpace(i);
        }

        if (m_start == -1) {
            return false;
        }
        m_end = getFirstSpace(m_start);
        return m_end != -1;
    }

    private int getFirstSpace(int indexIn) {
        int ind = indexIn;
        while (true) {
            if (ind >= m_length) {
                // hit end of string
                if (indexIn == ind) {
                    // no more words
                    return -1;
                }
                return ind;
            }
            if (m_string.charAt(ind) == Space) {
                return ind;
            }
            ind++;
        }
    }

    private int getFirstNonSpace(int indexIn) {
        int ind = indexIn;
        while (true) {
            if (ind >= m_length) {
                // hit end of string
                if (indexIn == ind) {
                    // no more words
                    return -1;
                }
                return ind;
            }
            if (m_string.charAt(ind) != Space) {
                return ind;
            }
            ind++;
        }
    }


    /////////////
    // TEST CODE
    ////////////
    public void test() {
        test("Hello");
        test(" Hello ");
        test(" Hello World  Where are    you  ");
    }

    private void test(String s) {
        setString(s);
        while (hasWord()) {
            String word = getWord();
            Log.streamer.debug(word);
            nextWord();
        }
    }
}
