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
package com.hitorro.util.basefile.fs.ftp;

import com.hitorro.util.basefile.fs.BaseFile;
import com.hitorro.util.core.ArrayUtil;
import com.hitorro.util.core.Log;
import com.hitorro.util.core.UTCDateUtil;
import com.hitorro.util.core.string.Fmt;
import com.hitorro.util.io.largedata.compressedstreams.CInputStream;
import com.hitorro.util.io.largedata.compressedstreams.COutputStream;
import com.hitorro.util.io.largedata.compressedstreams.InputInputStream;
import com.hitorro.util.io.largedata.compressedstreams.OutputOutputStream;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.function.Predicate;

/**
 *
 */
public class FileTranProtFile extends BaseFile<FileTranProtFile, FTPFileSystem> implements Comparable {
    private FTPFileSystem provider;
    private FTPFile file;

    FileTranProtFile(FTPFileSystem provider, FTPFile file, String absPath) {
        this.provider = provider;
        this.file = file;
        this.path = absPath;
    }

    @Override
    public boolean delete() {
        try {
            provider.client().deleteFile(path);
            return true;
        } catch (IOException e) {
            Log.ftpfs.error("Unable to delete directory %s %s %e", path, e, e);
        }
        return false;
    }

    public void touch() {
        file.setTimestamp(UTCDateUtil.calendarForDate(new Date()));
    }

    @Override
    public boolean exists() {
        FTPFile file = getFile();
        if (file == null) {
            return false;
        }
        return file != null;
    }

    @Override
    public String getAbsolutePath() {
        return path;
    }

    @Override
    public FileTranProtFile getChild(final String child) {
        return new FileTranProtFile(provider, null, Fmt.S("%s/%s", path, child));
    }

    @Override
    public boolean mkParentDir() {
        return this.getParent().mkdir();
    }

    @Override
    public InputStream getInputStreamRaw() throws IOException {
        FTPClient client = provider.client();
        client.setFileType(FTP.BINARY_FILE_TYPE);
        java.io.InputStream inputStream = new FileTransferProtocolInputStream(client.retrieveFileStream(path), client);
        checkInError();
        return inputStream;
    }

    @Override
    public OutputStream getOutputStreamRaw() throws IOException {
        return new DataOutputStream(provider.client().storeFileStream(path));
    }

    @Override
    public DataOutputStream getDataOutputStreamAppend() throws IOException {
        return new DataOutputStream(provider.client().appendFileStream(path));
    }

    @Override
    public FileTranProtFile getPeer(final String peer) {
        int index = path.lastIndexOf("/");
        if (index != -1) {
            String part = path.substring(0, index);
            return new FileTranProtFile(provider, null, Fmt.S("%s/%s", part, peer));
        }
        return null;
    }

    public FileTranProtFile getParentAux(String part) {
        return new FileTranProtFile(provider, null, part);
    }

    @Override
    public CInputStream getCInputStream() throws IOException {
        return new InputInputStream(getDataInputStream(), length());
    }

    @Override
    public long getModifiedTime() {
        FTPFile file = getFile();
        if (file == null) {
            return -1;
        }
        Calendar c = file.getTimestamp();
        if (c == null) {
            return -1;
        } else {
            return c.getTimeInMillis();
        }
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
    public FileTranProtFile getTempFile() {
        return new FileTranProtFile(provider, null, Fmt.S("%s.tmp", path));
    }

    /**
     * returns true if we didnt actually find the guy, you should do an exists check if you need a directory
     *
     * @return
     */
    @Override
    public boolean isDir() {
        FTPFile file = getFile();
        if (file == null) {
            return true;
        }
        return file.isDirectory();
    }

    @Override
    public boolean isLocal() {
        return false;
    }

    @Override
    public long length() {
        FTPFile file = getFile();
        if (file == null) {
            return 0;
        }
        return file.getSize();
    }

    @Override
    public FileTranProtFile[] listFiles() throws IOException {
        FTPFile files[] = provider.client().listFiles(path);
        if (ArrayUtil.nullOrEmpty(files)) {
            return null;
        }
        FileTranProtFile arr[] = new FileTranProtFile[files.length];
        for (int i = 0; i < files.length; i++) {
            arr[i] = new FileTranProtFile(provider, files[i], Fmt.S("%s/%s", path, files[i].getName()));
        }
        return arr;
    }

    @Override
    public FileTranProtFile[] listFiles(final Predicate<BaseFile> filter) throws IOException {
        FTPFile files[] = provider.client().listFiles(path);
        if (ArrayUtil.nullOrEmpty(files)) {
            return null;
        }
        List<FileTranProtFile> list = new ArrayList();
        for (FTPFile file : files) {
            FileTranProtFile slf = new FileTranProtFile(provider, file, Fmt.S("%s/%s", path, file.getName()));
            if (filter == null) {
                list.add(slf);
            } else if (filter.test(slf)) {
                list.add(slf);
            }
        }
        checkInError();
        return list.toArray(new FileTranProtFile[list.size()]);
    }

    @Override
    public boolean mkdir() {
        try {
            provider.client().makeDirectory(path);
            return true;
        } catch (IOException ioe) {
            Log.ftpfs.error("Cant mkdir %s %s %e", path, ioe, ioe);
            return false;
        }
    }

    @Override
    public boolean renameTo(final BaseFile replaceWithMe) throws IOException {
        return provider.client().rename(path, replaceWithMe.getAbsolutePath());
    }

    @Override
    public boolean replace(final BaseFile replaceWithMe) throws IOException {
        delete();
        return renameTo(replaceWithMe);
    }

    @Override
    public boolean setLastModified(final long time) {
        return false;
    }

    public int compareTo(final Object o) {
        if (o instanceof FileTranProtFile) {
            FileTranProtFile ff = (FileTranProtFile) o;
            return path.compareTo(ff.path);
        }
        return -1;
    }


    //******************************** internal helpers ****************************************

    public void checkInError() throws IOException {
        int code = provider.client().getReplyCode();
        if (FTPReply.isNegativeTransient(code) || FTPReply.isNegativePermanent(code)) {
            throw new IOException(Fmt.S("FTP Error code: %d\tMessage: %s", code, provider.client().getReplyString()));
        }
    }

    private FTPFile getFileFromPath(String path) throws IOException {
        FTPFile files[] = provider.client().listFiles(path);
        if (ArrayUtil.nullOrEmpty(files)) {
            return null;
        }
        if (files.length == 1) {
            return files[0];
        }
        return null;
    }

    private FTPFile getFile() {
        if (file != null) {
            return file;
        }
        try {
            file = getFileFromPath(path);
        } catch (IOException e) {
            Log.ftpfs.error("Unable to get file %s %s %e", path, e, e);
        }
        return file;
    }
}
