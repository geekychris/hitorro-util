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
package com.hitorro.util.io;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Wrapper around a file that keeps track of a couple basic stats such as modified time and size.  You can query to see
 * if this has changed.  Can report when files change in a set.  Will reset what was changed to the new values
 */
public class FileChangeContainer {
    private File file;

    private long modTime;
    private long tempModTime;
    private long size;
    private long tempSize;

    public FileChangeContainer(File file) {
        this.file = file;
        getTS();
        applyChanges();
    }

    public static List<FileChangeContainer> getChangesFromFileList(List<File> list) {
        List<FileChangeContainer> cont = new ArrayList();
        for (File f : list) {
            cont.add(new FileChangeContainer(f));
        }
        return cont;
    }

    private void getTS() {
        tempModTime = file.lastModified();
        tempSize = file.length();
    }

    public boolean hasChanged() {
        getTS();
        return isDifferent();
    }

    public boolean hasChangedReset() {
        if (hasChanged()) {
            applyChanges();
            return true;
        }
        return false;
    }

    private boolean isDifferent() {
        if (tempModTime != modTime) {
            return true;
        }
        if (tempSize != size) {
            return true;
        }
        return false;
    }

    private void applyChanges() {
        modTime = tempModTime;
        size = tempSize;
    }
}
