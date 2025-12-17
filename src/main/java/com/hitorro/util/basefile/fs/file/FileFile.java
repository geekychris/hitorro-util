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
import com.hitorro.util.core.Platform;
import com.hitorro.util.core.string.Fmt;
import com.hitorro.util.core.string.StringUtil;
import com.hitorro.util.io.FileUtil;
import com.hitorro.util.io.largedata.compressedstreams.CInputStream;
import com.hitorro.util.io.largedata.compressedstreams.COutputStream;
import com.hitorro.util.io.largedata.compressedstreams.FSInputStream;
import com.hitorro.util.io.largedata.compressedstreams.FSOutputStream;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * User: chris
 */
public class FileFile extends BaseFile<FileFile, FileFileSystem> implements Comparable {
    FileFileSystem ffs;
    private File completeFile;

    FileFile(FileFileSystem provider, String path) {
        this.ffs = provider;
        String root = ffs.getPathPart();
        if (StringUtil.nullOrEmptyString(root)) {
            completeFile = new File(ffs.getFileRoot(), path);
        } else {
            completeFile = new File(ffs.getFileRoot(), Fmt.S("%s/%s", ffs.getPathPart(), path));
        }
        this.path = path;
    }

    public boolean supportsSoftLink() {
        return true;
    }

    public void touch() {
        completeFile.setLastModified(System.currentTimeMillis());
    }

    public boolean linkTo(BaseFile bf) throws IOException {
        if (bf instanceof FileFile) {
            FileFile ff = (FileFile) bf;
            return Platform.getPlatform().softLink(completeFile, ff.completeFile);
        }

        return false;
    }

    public int compareTo(Object o) {
        if (o instanceof FileFile) {
            FileFile ff = (FileFile) o;
            return completeFile.compareTo(ff.completeFile);
        }
        return -1;
    }

    public boolean delete() {
        if (this.isDir()) {
            FileUtil.deleteDirectoryContent(getJavaFile(), true);
        } else {
            getJavaFile().delete();
        }
        return true;
    }

    public boolean exists() {
        return completeFile.exists();
    }

    public String getAbsolutePath() {
        return getJavaFile().getAbsolutePath();
    }

    public FileFile getChild(String child) {
        return new FileFile(ffs, Fmt.S("%s/%s", path, child));
    }

    public InputStream getInputStreamRaw() throws IOException {
        return FileUtil.getBufferedFileInputStream(completeFile);
    }

    public OutputStream getOutputStreamRaw() throws IOException {
        return FileUtil.getBufferedFileOutputStream(completeFile);
    }

    public DataOutputStream getDataOutputStreamAppend() throws IOException {
        return FileUtil.getDataOutputStreamForFileAppend(completeFile);
    }

    public File getJavaFile() {
        return completeFile;
    }

    public FileFile getPeer(String peer) {
        return getParent().getChild(peer);
    }

    public CInputStream getCInputStream() throws IOException {
        return new FSInputStream(completeFile);
    }

    public long getModifiedTime() {
        return completeFile.lastModified();
    }

    public COutputStream getCOutputStream() throws IOException {
        FileUtil.ensureParentDirectories(completeFile, true);
        return new FSOutputStream(completeFile);
    }

    public COutputStream getCOutputStreamAppend() throws IOException {
        FileUtil.ensureParentDirectories(completeFile, true);
        return new FSOutputStream(completeFile, true);
    }

    public FileFile getTempFile() {
        return new FileFile(ffs, Fmt.S("%s.tmp", path));
    }

    public boolean isDir() {
        return completeFile.isDirectory();
    }

    public boolean isLocal() {
        return true;
    }

    public long length() {
        return completeFile.length();
    }

    public FileFile[] listFiles(Predicate<BaseFile> filter) {
        File files[] = completeFile.listFiles();
        if (files == null) {
            return null;
        }
        List<BaseFile> afs = new ArrayList();
        for (int i = 0; i < files.length; i++) {
            BaseFile afTemp = new FileFile(ffs, Fmt.S("%s/%s", path, files[i].getName()));
            if (filter.test(afTemp)) {
                afs.add(afTemp);
            }
        }
        return afs.toArray(new FileFile[afs.size()]);
    }

    public boolean mkdir() {
        if (getJavaFile().exists()) {
            return true;
        }
        return FileUtil.ensureDirectoryExists(getJavaFile());
    }

    public boolean mkParentDir() {
        return FileUtil.ensureParentDirectories(this.completeFile, true);
    }

    public boolean renameTo(BaseFile replaceWithMe) throws IOException {
        replaceWithMe.delete();
        if (this.ffs == replaceWithMe.getFS()) {
            FileFile ff = (FileFile) replaceWithMe;
            this.completeFile.renameTo(ff.completeFile);
        } else {
            replaceWithMe.replace(this);
        }
        return true;
    }

    public File getLocalFileIfPossible() {
        return completeFile;
    }

    public boolean replace(BaseFile replaceWithMe) {
        if (replaceWithMe.isLocal()) {
            FileFile repFile = (FileFile) replaceWithMe;
            File rep = repFile.getJavaFile();
            if (!rep.exists()) {
                return false;
            }
            File me = getJavaFile();
            if (!me.exists()) {
                rep.renameTo(me);
                return true;
            }
            if (FileUtil.swap(rep, me)) {
                rep.delete();
            }
        }
        return false;
    }

    public boolean setLastModified(long time) {
        return getJavaFile().setLastModified(time);
    }

    public String toString() {
        return completeFile.toString();
    }

    public FileFile getParentAux(String part) {
        return new FileFile(ffs, part);
    }
}
