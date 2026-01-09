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
import java.io.FileFilter;

/**
 * Using the FileFilter interface we are called by such things as the listFiles function of a directory to accumulate
 * file size stats.  We would use something like du but that only does blocks.
 */
public class AccrueFileSizeFilter implements FileFilter {
    private String fileExtensionToAccrue;
    private long m_size = 0;
    private int filesProcessed = 0;
    private int filesWithExtension = 0;
    private boolean acceptIfFileMatch = false;

    public AccrueFileSizeFilter(String fileExtensionToAccrue, boolean acceptIfFileMatch) {
        this.fileExtensionToAccrue = fileExtensionToAccrue;
        this.acceptIfFileMatch = acceptIfFileMatch;
    }

    public boolean accept(File file) {
        filesProcessed++;
        if (file.getName().endsWith(fileExtensionToAccrue)) {
            if (file.isFile()) {
                filesWithExtension++;
                m_size += file.length();
                if (acceptIfFileMatch) {
                    return true;
                }
            }
        }
        return false;
    }

    public long getFileSize() {
        return m_size;
    }

    public int getFilesProcessed() {
        return filesProcessed;
    }

    public int getFilesMatched() {
        return filesWithExtension;
    }
}