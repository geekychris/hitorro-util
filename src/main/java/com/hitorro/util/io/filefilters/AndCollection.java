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
 * Allow ANDing of multiple Name filters.  Optimized to return false on the first subordinate that returns false.
 * <p/>
 * Requires three phases of use:
 * <p/>
 * 1) put 2) use in accept logic of a file.listFiles(FilenameFilter)
 *
 * @author chris
 */
public class AndCollection extends LogicalCollection {

    public AndCollection(FilenameFilter... filters) {
        super(filters);
    }

    @Override
    public boolean accept(File dir, String name) {

        if (tempFilters != null) {
            finalizeArray();
        }
        for (int i = 0; i < m_filters.length; i++) {
            if (!m_filters[i].accept(dir, name)) {
                return false;
            }
        }
        return true;
    }

}
