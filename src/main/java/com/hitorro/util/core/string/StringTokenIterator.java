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
package com.hitorro.util.core.string;

public class StringTokenIterator {
    private String text;
    private String dlms;        // null if a single char delimiter
    private char delimiterChar; // delimiter if a single char delimiter

    private String token;
    private int start;
    private int end;
    private boolean done;

    public StringTokenIterator(String text, String dlms) {
        this.text = text;
        if (dlms.length() == 1) {
            delimiterChar = dlms.charAt(0);
        } else {
            this.dlms = dlms;
        }
        setStart(0);
    }

    public String first() {
        setStart(0);
        return token;
    }

    public String current() {
        return token;
    }

    public int currentStart() {
        return start;
    }

    public int currentEnd() {
        return end;
    }

    public boolean isDone() {
        return done;
    }

    public String next() {
        if (hasNext()) {
            start = end + 1;
            end = nextDelimiter(start);
            token = text.substring(start, end);
        } else {
            start = end;
            token = null;
            done = true;
        }
        return token;
    }

    public boolean hasNext() {
        return (end < text.length());
    }

    public StringTokenIterator setStart(int offset) {
        if (offset > text.length()) {
            throw new IndexOutOfBoundsException();
        }
        start = offset;
        end = nextDelimiter(start);
        token = text.substring(start, end);
        done = false;
        return this;
    }

    public StringTokenIterator setText(String text) {
        this.text = text;
        setStart(0);
        return this;
    }

    private int nextDelimiter(int start) {
        int textlen = this.text.length();
        if (dlms == null) {
            for (int idx = start; idx < textlen; idx++) {
                if (text.charAt(idx) == delimiterChar) {
                    return idx;
                }
            }
        } else {
            int dlmslen = dlms.length();
            for (int idx = start; idx < textlen; idx++) {
                char c = text.charAt(idx);
                for (int i = 0; i < dlmslen; i++) {
                    if (c == dlms.charAt(i)) {
                        return idx;
                    }
                }
            }
        }
        return textlen;
    }
}