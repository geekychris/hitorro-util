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

import com.hitorro.util.core.string.StringUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 */
public class DirectoryScanner {
    private int basePathNameLen = 0;

    private List<String> fileList = new ArrayList<String>();
    private List<String> dirList = new ArrayList<String>();

    /**
     * indicates if the src dir entry makes it into the jar file.
     */
    private boolean includeSrcDir = true;

    public DirectoryScanner() {
    }

    public void setIncludeSrcDir(boolean state) {
        includeSrcDir = state;
    }

    /**
     * @return the parent directory
     */
    public String scan(File dir) {
        String parentDir = null;

        File topDir = dir;
        if (!topDir.exists()) {
            throw new IllegalArgumentException("File/dir not found: " + dir);
        }

        if (!topDir.isDirectory()) {
            throw new IllegalArgumentException("jar'ing files not supported: " + dir);
        }

        /**
         * To return the dir's parent dir:
         *  a. Get the absolutePath
         *  b. Get the basame of dir
         *  c. return the (absloutePath-basename)
         */
        String absPath = topDir.getAbsolutePath();
        int absLen = topDir.getAbsolutePath().length();

        String baseName = topDir.getName();
        int baseNameLen = baseName.length();

        if (includeSrcDir) {
            basePathNameLen = absLen - baseNameLen;
            parentDir = absPath.substring(0, basePathNameLen);
        } else {
            basePathNameLen = absLen;
            parentDir = baseName;
        }

        scanDir(topDir, true);


        return parentDir;
    }

    /**
     * Scan the directory and populate the files and directories listFiles
     *
     * @param dir      The path name to scan
     * @param topLevel true indicates that this is were started off with
     */
    private void scanDir(File dir, boolean topLevel) {
        int index = basePathNameLen;
        String absPath = dir.getAbsolutePath();

        if (!includeSrcDir) {
            // past the "/"
            index++;
        }

        if (dir.isDirectory()) {
            /**
             * Add the path name if includeSrcDir or if recursing.
             */
            if (includeSrcDir || !topLevel) {
                String nameToAdd = absPath.substring(index);
                dirList.add(nameToAdd);
            }

            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                scanDir(new File(dir, children[i]), false);
            }
        } else {
            String nameToAdd = absPath.substring(index);
            fileList.add(nameToAdd);
        }

        return;
    }

    public String[] getIncludedDirectories() {
        int numDirs = dirList.size();
        if (numDirs <= 0) {
            return null;
        }
        return StringUtil.toArray(dirList);
    }

    public String[] getIncludedFiles() {
        int numFiles = fileList.size();
        if (numFiles <= 0) {
            return null;
        }
        return StringUtil.toArray(fileList);
    }
}

