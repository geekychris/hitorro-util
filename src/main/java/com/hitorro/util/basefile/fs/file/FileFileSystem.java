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
package com.hitorro.util.basefile.fs.file;

import com.hitorro.util.basefile.fs.BaseFile;
import com.hitorro.util.basefile.fs.BaseFileSystem;
import com.hitorro.util.basefile.fs.configfactories.FileConfig;
import com.hitorro.util.core.ListUtil;
import com.hitorro.util.core.string.Fmt;
import com.hitorro.util.io.FileUtil;

import java.io.File;
import java.util.List;

/**
 * User: chris
 */
public class FileFileSystem extends BaseFileSystem<FileFile, FileFileSystem> {
    /**
     * For a file system you want from /
     */
    public static FileFileSystem Root = new FileFileSystem(new File("/"));

    private File root;

    public FileFileSystem(FileConfig conf) {
        root = new File(conf.getPath());
    }

    public FileFileSystem(File basePath) {
        this.root = basePath;
        this.pathPart = "";
    }

    public String toString() {
        return Fmt.S("FileFileSystem path: %s", root.getAbsolutePath());
    }

    public FileFile getFile(String path) {
        return new FileFile(this, path);
    }

    public FileFile getFile(BaseFile af) {
        return new FileFile(this, af.getRelativePath());
    }

    public FileFile getFileEnsuringDir(String path) {
        FileFile af = new FileFile(this, path);
        File f = af.getJavaFile();
        FileUtil.ensureParentDirectories(f, true);
        return af;
    }

    public boolean deleteFileSystem() {
        List<File> errorList = FileUtil.deleteDirectoryContent(this.root, false);
        return ListUtil.nullOrEmpty(errorList);
    }

    File getFileRoot() {
        return root;
    }
}
