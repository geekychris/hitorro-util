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
package com.hitorro.util.basefile.fs;

import com.hitorro.util.core.Console;

/**
 * Cascading set of file systems.  Used to mask where data comes from on a read operation.  One can imagine that you
 * have a "fileSystem" on file and one locally, or maybe on a master disk and one on a secondary.  Or you are using them
 * as configuration overides, one in HOME, one in BIN. This mechanism allows you to get a file from the first place it
 * can find it in a specified seek order.
 * <p/>
 * User: chris
 */
public class FileSystemCascadingContainer {
    private BaseFileSystem fileSystems[];

    public FileSystemCascadingContainer(BaseFileSystem providers[]) {
        this.fileSystems = providers;
    }

    public BaseFile getFileIfExists(String path, boolean supressCascade) {
        if (supressCascade) {
            BaseFile bf = fileSystems[fileSystems.length - 1].getFileIfExists(path);
            if (bf != null) {
                return bf;
            }
        }
        for (BaseFileSystem p : fileSystems) {
            BaseFile bf = p.getFileIfExists(path);
            if (bf != null) {
                return bf;
            }
        }
        return null;
    }

    public BaseFile getFileForWrite(String path) {
        return fileSystems[0].getFileEnsuringDir(path);
    }

    /**
     * Given a file in one tier, will provide the file in the other tier.  This is effectively "give me the relative
     * path of <fs1:path> as <fs2:path>.
     * <p/>
     * This implementation is somewhat naive and assumes a hdfs and a local file system
     *
     * @param bf
     * @return
     */
    public BaseFile getPeerFile(BaseFile bf) {
        int i = 0;
        if (fileSystems.length == 2) {
            if (fileSystems[i].isLocalFileSystem() == bf.isLocal()) {
                i = 1;
            }
            return fileSystems[i].getFile(bf.path);
        }
        return null;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (fileSystems != null) {
            for (BaseFileSystem p : fileSystems) {
                Console.bprintln(sb, p.toString());
            }
            return sb.toString();
        }
        return "Not set";
    }

}
