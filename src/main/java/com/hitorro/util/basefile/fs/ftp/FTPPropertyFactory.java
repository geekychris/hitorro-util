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

import com.fasterxml.jackson.databind.JsonNode;
import com.hitorro.util.basefile.fs.configfactories.BaseFilePropertyFactory;
import com.hitorro.util.basefile.fs.configfactories.FileSystemConfig;
import com.hitorro.util.json.keys.StringProperty;

/**
 *
 */
public class FTPPropertyFactory extends BaseFilePropertyFactory<FTPConfig, FileTranProtFile> {
    public static final String FTP = "ftp";


    public static StringProperty UserNameKey = new StringProperty("username", "Username to login as", null);
    public static StringProperty PasswordKey = new StringProperty("password", "Password to login as", null);
    public static StringProperty HostKey = new StringProperty("host", "Host to login to", null);

    public String[] getNames() {
        return new String[]{"ftpconfig"};
    }

    public FTPConfig getInstance(final JsonNode map, final String type, final String parentPathName) {
        FTPConfig ftp = new FTPConfig();
        ftp.userName = UserNameKey.apply(map);
        ftp.password = PasswordKey.apply(map);
        ftp.host = HostKey.apply(map);
        return ftp;
    }

    public String getProtocol() {
        return FTP;
    }

    /**
     * Assumes format of:
     * <p>
     * ftp://@@host/username/password@@/path
     *
     * @param parts
     * @return
     */

    public FileSystemConfig getConfigFromParts(String parts[]) {
        return new FTPConfig(parts[0], parts[1], parts[2]);
    }
}