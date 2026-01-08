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
package com.hitorro.util.core;

/**
 */
public class CharArrayWrapper implements PooledStorageElement {
    private int size = 0;
    private char array[];

    private CharArrayWrapper() {

    }

    private CharArrayWrapper(char[] arrayIn) {
        size = arrayIn.length;
        array = arrayIn;
    }

    public static CharArrayWrapper getWrapper(int initialSize) {
        return new CharArrayWrapper(new char[initialSize]);
    }

    public int getSize() {
        return size;
    }

    public void setSize(int sizeIn) {
        size = sizeIn;
    }

    public char[] getArray() {
        return array;
    }

    public int getRealStorageSize() {
        return array.length;
    }

    public void activate() {

    }

    public void pasivate() {
        ArrayUtil.nullOutCharArray(array);
        // return to free storage here!
    }


}
