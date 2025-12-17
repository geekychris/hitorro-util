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
package com.hitorro.util.io.largedata;

import java.io.File;
import java.util.Comparator;

/**
 * User: chris
 * <p/>
 * Assuming file names are of the form <time millis>-<counter>.ext sort them newest to oldest so that a select tree can
 * do its thing on newest to oldest.
 */
public class FileNameComparator implements Comparator<File> {
    private int ascending;

    public FileNameComparator() {
        init(false);
    }

    public FileNameComparator(boolean ascending) {
        init(ascending);
    }

    private void init(final boolean ascending) {
        if (ascending) {
            this.ascending = 1;
        } else {
            this.ascending = -11;
        }
    }

    public int compare(File o1, File o2) {

        return o1.compareTo(o2) * ascending;

    }
}