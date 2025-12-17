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
package com.hitorro.util.basefile.fs.jarfile;

import com.hitorro.util.basefile.fs.BaseFile;
import com.hitorro.util.io.largedata.compressedstreams.CInputStream;
import com.hitorro.util.io.largedata.compressedstreams.COutputStream;
import com.hitorro.util.io.largedata.compressedstreams.InputInputStream;
import com.hitorro.util.io.largedata.compressedstreams.OutputOutputStream;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.attribute.FileTime;
import java.util.function.Predicate;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Brain simple BaseFile encapsulating a zip entry.  Its limited because you can only get an input stream from it
 * currently.
 */
public class JarFileFile extends BaseFile<JarFileFile, JarFileSystem> implements Comparable {
    private ZipInputStream zis;
    private ZipEntry ze;

    JarFileFile(ZipInputStream zis, ZipEntry ze) {
        this.zis = zis;
        this.ze = ze;
        path = ze.getName();
    }


    public void touch() {
        FileTime ft = FileTime.fromMillis(System.currentTimeMillis());
        ze.setLastModifiedTime(ft);
    }

    @Override
    public boolean delete() {
        return false;
    }

    @Override
    public boolean exists() {
        return true;
    }

    @Override
    public String getAbsolutePath() {
        return path;
    }

    @Override
    public JarFileFile getChild(final String child) {
        return null;
    }

    @Override
    public boolean mkParentDir() {
        return false;
    }

    @Override
    public InputStream getInputStreamRaw() {
        // control the close method else the zis gets closed prematurely (close on ZIS interacts badly with callers who
        // think its just a stream)
        return new ZipInputWrapper(zis);
    }

    @Override
    public OutputStream getOutputStreamRaw() {
        return null;
    }

    @Override
    public DataOutputStream getDataOutputStreamAppend() {
        return null;
    }

    @Override
    public JarFileFile getPeer(final String peer) {
        return null;
    }

    public JarFileFile getParentAux(String part) {
        return null;
    }

    @Override
    public CInputStream getCInputStream() throws IOException {
        return new InputInputStream(getDataInputStream(), length());
    }

    @Override
    public long getModifiedTime() {
        return ze.getTime();
    }

    @Override
    public COutputStream getCOutputStream() throws IOException {
        return new OutputOutputStream(getDataOutputStream());
    }

    @Override
    public COutputStream getCOutputStreamAppend() throws IOException {
        return new OutputOutputStream(getDataOutputStreamAppend());
    }

    @Override
    public JarFileFile getTempFile() {
        return null;
    }

    /**
     * returns true if we didnt actually find the guy, you should do an exists check if you need a directory
     *
     * @return
     */
    @Override
    public boolean isDir() {
        return false;
    }

    @Override
    public boolean isLocal() {
        return false;
    }

    @Override
    public long length() {
        return ze.getSize();
    }

    @Override
    public JarFileFile[] listFiles() {
        return null;
    }

    @Override
    public JarFileFile[] listFiles(final Predicate<BaseFile> filter) {
        return null;
    }

    @Override
    public boolean mkdir() {
        return false;
    }

    @Override
    public boolean renameTo(final BaseFile replaceWithMe) {
        return false;
    }

    @Override
    public boolean replace(final BaseFile replaceWithMe) {
        return false;
    }

    @Override
    public boolean setLastModified(final long time) {
        return false;
    }

    public int compareTo(final Object o) {
        if (o instanceof JarFileFile) {
            JarFileFile ff = (JarFileFile) o;
            return path.compareTo(ff.path);
        }
        return -1;
    }

}
