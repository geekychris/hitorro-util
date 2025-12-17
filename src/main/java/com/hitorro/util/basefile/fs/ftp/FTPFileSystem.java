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
import com.hitorro.util.basefile.fs.BaseFileSystem;
import com.hitorro.util.core.Console;
import com.hitorro.util.core.Log;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;

import java.io.IOException;

/**
 *
 */
public class FTPFileSystem extends BaseFileSystem<FileTranProtFile, FTPFileSystem> {
    private FTPClient client = new FTPClient();
    private FTPConfig config;

    public FTPFileSystem(FTPConfig ftpConfig) throws IOException {
        this.config = ftpConfig;
        connect(config.getHost(), config.getUserName(), config.getPassword());
    }

    public static void testBasic() throws IOException {
        FTPConfig config = new FTPConfig("ftp.riken.jp", "anonymous", "chris@hitorro.com");
        FTPFileSystem sys = new FTPFileSystem(config);
        BaseFile bf = sys.getFile("/Linux/centos/6.0/isos/x86_64");
        BaseFile files[] = bf.listFiles();
        for (BaseFile f : files) {
            Console.println("%s, %s, age: %s modified: %s, size:%s", f.getName(), f.getAbsolutePath(), f.ageOfFile() / 1000, f.getModifiedTime(), f.length());
        }
        Console.println("done");
    }

    public boolean connect(String host, String userName, String password) throws IOException {
        client.connect(host);
        int code = client.getReplyCode();
        if (!FTPReply.isPositiveCompletion(code)) {
            Log.ftpfs.error("Unable to connect, error code: %s, %s", code, client.getReplyString());
            return false;
        }
        client.login(userName, password);
        client.setFileType(FTP.BINARY_FILE_TYPE);

        return true;
    }

    @Override
    public boolean deleteFileSystem() {
        return false;
    }

    @Override
    public FileTranProtFile getFile(final BaseFile af) {
        return new FileTranProtFile(this, null, af.getRelativePath());
    }

    @Override
    public FileTranProtFile getFile(final String path) {
        return new FileTranProtFile(this, null, path);
    }

    @Override
    public FileTranProtFile getFileEnsuringDir(final String path) {
        return getFile(path);
    }

    FTPClient client() {
        return client;
    }
}
