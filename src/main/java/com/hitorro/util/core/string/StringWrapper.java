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

import com.hitorro.util.io.largedata.compressedstreams.CInputStream;
import com.hitorro.util.io.largedata.compressedstreams.COutputStream;

import java.io.IOException;

public class StringWrapper {
    private int length;
    private char array[];

    public StringWrapper() {
        array = new char[5];
        length = 0;
    }

    public StringWrapper(String s) {
        array = new char[s.length()];
        length = 0;
        set(s);
    }

    public void set(String s) {
        set(s, 0, s.length());
    }

    public void set(String s, int start, int length) {
        ensureCapacitity(start, length);
        for (int i = 0; i < length; i++) {
            array[i + start] = s.charAt(i);
        }
    }

    public void write(COutputStream outs, int start) throws IOException {
        outs.writeVInt(length - start);
        outs.writeChars(array, start, length - start);
    }

    public boolean read(CInputStream in, int start) throws IOException {
        if (start > length) {
            return false;
        }
        int lengthToRead = in.readVInt();
        ensureCapacitity(start, lengthToRead);
        in.readChars(array, length, lengthToRead);
        length = start + lengthToRead;
        return true;
    }

    public int length() {
        return length;
    }

    public void prune(int l) {
        if (length > l) {
            length = l;
        }
    }

    public int commonLength(StringWrapper sw) {
        int common = Math.min(length, sw.length);
        for (int i = 0; i < common; i++) {
            if (array[i] != sw.array[i]) {
                return i;
            }
        }
        return common;
    }

    public void append(char buff[], int start, int endIn) {
        int appendLength = endIn - start;
        ensureCapacitity(start, appendLength);
        int runLengthStart = appendLength + length;
        for (int i = 0; i < appendLength; i++) {
            array[runLengthStart + i] = buff[start + i];
        }
        length += appendLength;
    }

    private void ensureCapacitity(final int start, final int appendLength) {
        array = ensureCapacity(appendLength + start,
                length, array);
    }

    private final char[] ensureCapacity(int l, int oldPartsLength, char oldParts[]) {
        if (oldParts.length < l) {
            char newPart[] = new char[l * 2];
            for (int i = 0; i < oldPartsLength; i++) {
                newPart[i] = oldParts[i];
            }
            return newPart;
        }
        return oldParts;
    }
}
