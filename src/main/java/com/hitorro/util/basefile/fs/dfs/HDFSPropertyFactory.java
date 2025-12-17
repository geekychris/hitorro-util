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

import com.fasterxml.jackson.databind.JsonNode;
import com.hitorro.util.basefile.fs.BaseFile;
import com.hitorro.util.basefile.fs.configfactories.BaseFilePropertyFactory;
import com.hitorro.util.basefile.fs.configfactories.FileSystemConfig;
import com.hitorro.util.json.keys.StringProperty;

import java.io.IOException;

/**
 *
 */
public class HDFSPropertyFactory extends BaseFilePropertyFactory<HDFSConfig, DFSFile> {
    public static final String HDFS = "hdfs";

    public static StringProperty HdfsUriKey = new StringProperty("hdfsuri", "hdfs uri", null);
    public static StringProperty RootPathKey = new StringProperty("rootpath", "Root path", null);

    public String[] getNames() {
        return new String[]{"hdfsconfig"};
    }

    public HDFSConfig getInstance(final JsonNode map, final String type, final String parentPathName) {
        HDFSConfig dfs = new HDFSConfig();
        dfs.hdfsURI = HdfsUriKey.apply(map);
        dfs.rootPath = RootPathKey.apply(map);
        return dfs;
    }

    public String getProtocol() {
        return HDFS;
    }

    public BaseFile getBaseFileFromPath(String val) throws IOException {
        BaseFile bf = super.getBaseFileFromPath(val);
        if (bf != null) {
            return bf;
        }
        // didnt see any magic config stuff, going to assume that the FS can handle directly the url.
        DFSFileSystem prov = new DFSFileSystem(val);
        return prov.getFile("");
    }

    @Override
    public FileSystemConfig getConfigFromParts(final String[] parts) {
        HDFSConfig hConf = new HDFSConfig();
        hConf.hdfsURI = parts[0];
        hConf.rootPath = parts[1];
        return hConf;
    }
}

