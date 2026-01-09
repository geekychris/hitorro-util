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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * <p/>
 * Recurse a filepath in an efficient way (this is really a cursor over a nested directory
 */
public class FilePathIterator extends AbstractIterator<String> {
    private List<String> m_files = new ArrayList<String>();
    private int removeLeft;
    private String path;

    public FilePathIterator(String path, boolean includeJars) {
        removeLeft = path.length();
        recursiveList(path);
        this.path = path;
    }

    private void recursiveList(String path) {
        File f = new File(path);
        File files[] = f.listFiles();
        if (files == null) {
            return;
        }
        for (File file : files) {
            if (file.isDirectory()) {
                recursiveList(file.getAbsolutePath());
            } else {
                String v = file.getAbsolutePath().substring(removeLeft + 1);
                m_files.add(v);
            }
        }
    }

    public boolean hasNext() {
        return m_files.size() > 0;
    }

    public String next() {
        String returnMe = m_files.get(m_files.size() - 1);
        m_files.remove(m_files.size() - 1);
        return returnMe;
    }

    public void remove() {
    }

    @Override
    public void close() throws Exception {
    }
}
