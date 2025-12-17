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
package com.hitorro.util.io.filefilters;

import java.io.File;
import java.io.FilenameFilter;

/**
 * Match filenames that have an exact char test....not a very usefull class if you ask me.
 *
 * @author chris
 */
public class FileNameFilter implements FilenameFilter {
    private String m_matchMe;
    private boolean m_ignoreCase;

    /**
     * @param matchMe    string to test whole filename to
     * @param ignoreCase true if we wish to ignore case
     */
    public FileNameFilter(String matchMe, boolean ignoreCase) {
        m_matchMe = matchMe;
        this.m_ignoreCase = ignoreCase;
    }

    public boolean accept(File dir, String name) {
        if (m_ignoreCase) {
            return name.equalsIgnoreCase(m_matchMe);
        }
        return name.equals(m_matchMe);
    }
}

