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
import java.io.StreamTokenizer;

public class StreamTokenizerIterator extends AbstractIterator<String> {
    private StreamTokenizer m_tokenizer;

    private String m_token;

    private boolean hasToken;

    public StreamTokenizerIterator(StreamTokenizer tokenizer) {
        m_tokenizer = tokenizer;
        getToken();
    }

    public boolean hasNext() {
        return hasToken;
    }

    public String next() {
        String o = m_token;
        getToken();
        return o;
    }

    public void remove() {

    }

    private boolean getToken() {
        try {
            if (m_tokenizer.nextToken() != StreamTokenizer.TT_EOF) {
                hasToken = true;
                switch (m_tokenizer.ttype) {

                    case StreamTokenizer.TT_NUMBER:
                        m_token = Double.toString(m_tokenizer.nval);
                        break;
                    case StreamTokenizer.TT_WORD:
                        m_token = m_tokenizer.sval; // Already a String
                        break;
                    default: // single character in ttype
                        m_token = String.valueOf((char) m_tokenizer.ttype);
                }
            } else {
                hasToken = false;
            }
        } catch (IOException e) {
            hasToken = false;
        }
        return hasToken;
    }

    @Override
    public void close() throws Exception {
    }
}
