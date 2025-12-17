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
package com.hitorro.util.basefile.fs.dfs;

import com.hitorro.util.basefile.fs.BaseFile;
import com.hitorro.util.core.ArrayUtil;
import com.hitorro.util.core.Log;
import com.hitorro.util.core.string.Fmt;
import com.hitorro.util.core.string.StringUtil;
import com.hitorro.util.io.largedata.compressedstreams.CInputStream;
import com.hitorro.util.io.largedata.compressedstreams.COutputStream;
import com.hitorro.util.io.largedata.compressedstreams.InputInputStream;
import com.hitorro.util.io.largedata.compressedstreams.OutputOutputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * User: chris
 */
public class DFSFile extends BaseFile<DFSFile, DFSFileSystem> {
    private FileSystem hdfsFileSystem;
    private Path fileSystemPath;
    private FileStatus stat;

    public DFSFile(FileSystem fs, DFSFileSystem provider, String path) {
        init(provider, fs, path, null);
    }

    public DFSFile(FileSystem fs, DFSFileSystem provider, String path, FileStatus stat) {
        init(provider, fs, path, stat);
    }

    public boolean delete() throws IOException {
        hdfsFileSystem.delete(fileSystemPath, true);
        return true;
    }

    public void touch() {
        Path p = getPath();
        FileStatus f = getStatus();
        if (f == null) {
            return;
        }
        long accessTime = f.getAccessTime();
        try {
            hdfsFileSystem.setTimes(p, System.currentTimeMillis(), accessTime);
        } catch (IOException e) {
            Log.filesystem.error("Unable to touch file %s %s %e", fileSystemPath, e, e);
        }
    }

    public boolean exists() {
        try {
            return hdfsFileSystem.exists(fileSystemPath);
        } catch (IOException e) {
            Log.filesystem.error("Unable to verify existence of %s %s %e", path, e, e);
        }
        return false;
    }

    public String getAbsolutePath() {
        return fileSystemPath.toString();
    }

    public DFSFile getChild(String child) {
        return new DFSFile(hdfsFileSystem, getFS(), Fmt.S("%s/%s", this.path, child));
    }

    public InputStream getInputStreamRaw() throws IOException {
        return hdfsFileSystem.open(fileSystemPath);
    }

    public OutputStream getOutputStreamRaw() throws IOException {
        return hdfsFileSystem.create(fileSystemPath);
    }

    public DataOutputStream getDataOutputStreamAppend() throws IOException {
        return hdfsFileSystem.append(fileSystemPath);
    }

    public CInputStream getCInputStream() throws IOException {
        return new InputInputStream(hdfsFileSystem.open(fileSystemPath), length());
    }

    public DFSFile getParentAux(String part) {
        return new DFSFile(hdfsFileSystem, getFS(), part);
    }

    public long getModifiedTime() {
        FileStatus f = getStatus();
        if (f == null) {
            return -1;
        }
        return f.getModificationTime();
    }

    public COutputStream getCOutputStream() throws IOException {
        return new OutputOutputStream(hdfsFileSystem.create(fileSystemPath));
    }

    public COutputStream getCOutputStreamAppend() throws IOException {
        return new OutputOutputStream(hdfsFileSystem.append(fileSystemPath));
    }

    public Path getPath() {
        return fileSystemPath;
    }

    private FileStatus getStatus() {
        if (stat == null) {
            try {
                stat = hdfsFileSystem.getFileStatus(fileSystemPath);
            } catch (IOException e) {
                Log.filesystem.error("Unable to get Hadoop filestatus for %s %s %e", path, e, e);
            }
        }
        return stat;
    }

    public DFSFile getTempFile() {
        return new DFSFile(hdfsFileSystem, getFS(), Fmt.S("%s.tmp", path));
    }

    public DFSFile getPeer(String peer) {
        return new DFSFile(hdfsFileSystem, getFS(), peer);
    }

    private void init(DFSFileSystem provider, FileSystem fs, String pathIn, FileStatus statIn) {
        this.setFS(provider);
        this.hdfsFileSystem = fs;
        path = pathIn;
        stat = statIn;

        if (StringUtil.nullOrEmptyString(provider.getPathPart())) {
            fileSystemPath = new Path(path);
        } else {
            fileSystemPath = new Path(Fmt.S("%s/%s", provider.getPathPart(), path));
        }
    }

    public boolean isDir() {
        FileStatus f = getStatus();
        return null != f && f.isDir();
    }

    public long length() {
        FileStatus f = getStatus();
        if (f == null) {
            return -1;
        }
        return f.getLen();
    }

    public DFSFile[] listFiles(Predicate<BaseFile> filter) throws IOException {
        if (hdfsFileSystem == null) {
            return null;
        }
        FileStatus fileStats[] = hdfsFileSystem.listStatus(fileSystemPath);
        if (ArrayUtil.nullOrEmpty(fileStats)) {
            return null;
        }
        List<DFSFile> files = new ArrayList();
        for (int i = 0; i < fileStats.length; i++) {
            DFSFile t = new DFSFile(hdfsFileSystem, getFS(), Fmt.S("%s/%s", this.path, fileStats[i].getPath().getName()), fileStats[i]);
            if (filter.test(t)) {
                files.add(t);
            }
        }
        return files.toArray(new DFSFile[files.size()]);
    }

    public boolean mkParentDir() {
        try {
            return hdfsFileSystem.mkdirs(fileSystemPath.getParent());
        } catch (IOException e) {
            return true;
        }
    }

    public boolean mkdir() {
        try {
            return hdfsFileSystem.mkdirs(fileSystemPath);
        } catch (IOException e) {
            return false;
        }
    }

    public boolean renameTo(BaseFile replaceWithMe) throws IOException {
        replaceWithMe.delete();
        if (!replaceWithMe.getFS().isLocalFileSystem()) {
            DFSFile ff = (DFSFile) replaceWithMe;
            return this.hdfsFileSystem.rename(this.fileSystemPath, ff.fileSystemPath);
        } else {
            replaceWithMe.replace(this);
        }
        return true;
    }

    /**
     * @param replaceWithMe
     * @return
     */
    public boolean replace(BaseFile replaceWithMe) throws IOException {
        if (!replaceWithMe.isLocal()) {
            DFSFile repFile = (DFSFile) replaceWithMe;
            if (hdfsFileSystem.exists(fileSystemPath)) {
                hdfsFileSystem.delete(fileSystemPath, true);
            }
            return hdfsFileSystem.rename(repFile.fileSystemPath, this.fileSystemPath);
        }
        return false;
    }

    public boolean setLastModified(long time) throws IOException {
        hdfsFileSystem.setTimes(fileSystemPath, time, -1);
        return true;
    }

    public String toString() {
        return fileSystemPath.toString();
    }
}