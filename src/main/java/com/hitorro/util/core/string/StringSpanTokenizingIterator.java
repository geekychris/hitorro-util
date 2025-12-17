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

import com.hitorro.util.core.iterator.AbstractIterator;


public class StringSpanTokenizingIterator extends AbstractIterator<StringRange> {
    private StringTokenIterator sti;
    private StringRange curr;
    private String s;

    public StringSpanTokenizingIterator(String s, String sep) {
        this.s = s;
        sti = new StringTokenIterator(s, sep);
        curr = new StringRange(s, sti.currentStart(), sti.currentEnd());
    }

    @Override
    public boolean hasNext() {
        return curr != null;
    }

    @Override
    public StringRange next() {
        String c = sti.next();
        if (c == null) {
            return null;
        }
        return new StringRange(s, sti.currentStart(), sti.currentEnd());
    }

    public String getRest() {
        return s.substring(sti.currentStart());
    }

    @Override
    public void remove() {

    }
}

