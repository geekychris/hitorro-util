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
import com.hitorro.util.basefile.fs.BaseFileSystem;
import com.hitorro.util.core.Log;
import com.hitorro.util.core.string.Fmt;
import com.hitorro.util.json.keys.StringProperty;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.security.UserGroupInformation;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * User: chris
 */
public class DFSFileSystem extends BaseFileSystem<DFSFile, DFSFileSystem> {
    public static final StringProperty HDFSNameServerUri = new StringProperty("filesystem.ns.uri", "uri for filesystem name server", "hdfs://localhost:9000/");

    public static final StringProperty DFSUserName = new StringProperty("filesystem.username", "", "bob");
    public static final StringProperty DFSGroupName = new StringProperty("filesystem.groupname", "", "chris");


    protected String rootPath;
    protected String hdfsURI;

    protected FileSystem fs = null;
    protected boolean triedAndFailed = false;

    public DFSFileSystem(String rootPath) {
        this(HDFSNameServerUri.apply(), rootPath);
    }

    public DFSFileSystem(HDFSConfig config) {
        this(config.getHdfsURI(), config.getRootPath());
    }

    public DFSFileSystem(String hdfsURI, String rootPath) {
        this.hdfsURI = hdfsURI;
        this.rootPath = rootPath;
    }

    public boolean deleteFileSystem() throws IOException {
        return getHDFSFileSystem().delete(getRootPath(), true);
    }

    public DFSFile getFile(BaseFile af) {
        FileSystem fs = getHDFSFileSystem();
        if (fs == null) {
            return null;
        }

        return new DFSFile(fs, this, af.getRelativePath());
    }

    public DFSFile getFile(String path) {
        FileSystem fs = getHDFSFileSystem();
        if (fs == null) {
            return null;
        }

        return new DFSFile(fs, this, path);
    }

    public DFSFile getFileEnsuringDir(String path) {
        return getFile(path);
    }

    public String getPathPart() {
        return rootPath;
    }

    public Path getRootPath() {
        return new Path(rootPath);
    }

    public boolean isLocalFileSystem() {
        return false;
    }

    public String toString() {
        return Fmt.S("DFSFileSystem file path: %s", rootPath);
    }

    public boolean isFileSystemAvailable() {
        return getHDFSFileSystem() != null;
    }

    public FileSystem getHDFSFileSystem() {
        if (!triedAndFailed) {
            if (fs == null) {
                fs = getFileSystem();
            }
        }
        return fs;
    }

    protected FileSystem getFileSystem() {
        if (!HDFSEnabled.apply()) {
            return null;
        }
        FileSystem ret = null;

        try {
            URI uri = new URI(hdfsURI);

            Configured c = new Configured();

            Configuration conf = new Configuration(false);
            //UserGroupInformation ugi =UserGroupInformation.createRemoteUser("Chris");
            conf.setStrings("hadoop.job.ugi", DFSUserName.apply(), DFSGroupName.apply());
            // conf.set("fs.default.name", "hdfs://192.168.1.180:54311");
            //conf.set("fs.hdfs.impl", Hdfs.class.getCanonicalName());
            conf.set("fs.hdfs.impl", org.apache.hadoop.hdfs.DistributedFileSystem.class.getCanonicalName());
            // need todo this to overide kerberos (it blows up)
            UserGroupInformation.setConfiguration(conf);
            ret = FileSystem.get(uri, conf);
            triedAndFailed = false;
            return ret;
        } catch (IOException e) {
            Log.util.error("Unable to connect to filesystem %s %s %e", hdfsURI, e, e);
            triedAndFailed = true;
        } catch (URISyntaxException e) {
            Log.util.error("URI for filesystem name server incorrectly formed %s %e, e", hdfsURI, e, e);
            triedAndFailed = true;
        }

        return ret;
    }


}
