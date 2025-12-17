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

import com.fasterxml.jackson.databind.JsonNode;
import com.hitorro.util.basefile.fs.configfactories.BaseFilePropertyFactory;
import com.hitorro.util.basefile.fs.configfactories.FilePropertyFactory;
import com.hitorro.util.basefile.fs.dfs.DFSFileSystem;
import com.hitorro.util.basefile.fs.dfs.HDFSPropertyFactory;
import com.hitorro.util.basefile.fs.file.FileFileSystem;
import com.hitorro.util.basefile.fs.ftp.FTPPropertyFactory;
import com.hitorro.util.basefile.fs.s3.S3PropertyFactory;
import com.hitorro.util.core.string.Fmt;
import com.hitorro.util.json.keys.BooleanProperty;
import com.hitorro.util.json.keys.FileProperty;
import com.hitorro.util.json.keys.StringProperty;
import com.hitorro.util.propertykeys.complex.ComplexPropertyContext;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

/**
 * User: chris
 */
public abstract class BaseFileSystem<F extends BaseFile, S extends BaseFileSystem> {
    public static final StringProperty HDFSNameSpace = new StringProperty("filesystem.subspace", "namespace within HDFS for data", "ht");
    public static final BooleanProperty HDFSEnabled = new BooleanProperty("filesystem.enabled", "namespace within HDFS for data", false);

    public static final FileProperty FakeHDFS = new FileProperty("filesystem.fake.dir", "Fake location for filesystem using the local FS", "");

    private static HashMap<String, ProtocolAdapter> adapters = getInitialAdapters();

    private static boolean configFactories = getInitializeConfigFactories();
    protected String pathPart;

    private static HashMap<String, ProtocolAdapter> getInitialAdapters() {
        HashMap<String, ProtocolAdapter> a = new HashMap();
        registerFS(a, new S3PropertyFactory());
        registerFS(a, new HDFSPropertyFactory());
        registerFS(a, new FTPPropertyFactory());
        registerFS(a, new FilePropertyFactory());
        return a;
    }

    private static boolean getInitializeConfigFactories() {
        ComplexPropertyContext.add(new S3PropertyFactory());
        ComplexPropertyContext.add(new HDFSPropertyFactory());
        ComplexPropertyContext.add(new FTPPropertyFactory());
        return true;
    }

    private static boolean registerFS(HashMap<String, ProtocolAdapter> a, BaseFilePropertyFactory factory) {
        ComplexPropertyContext.add(factory);
        a.put(factory.getProtocol(), factory);
        return true;
    }

    public static void addProtocolAdapter(ProtocolAdapter pa) {
        adapters.put(pa.getProtocol().toLowerCase(), pa);
    }

    public static ProtocolAdapter getProtocolAdapter(String name) {
        int index = name.indexOf(":");
        if (index != -1) {
            name = name.substring(0, index);
        }
        name = name.toLowerCase();
        return adapters.get(name);
    }

    public static BaseFile getBaseFileFromPath(final String val) throws IOException {
        ProtocolAdapter pa = getProtocolAdapter(val);
        if (pa != null) {
            return pa.getBaseFileFromPath(val);
        }
        // relative path
        BaseFileSystem prov = getGlobalNamespaceFileSystem("");
        return prov.getFile(val);
    }

    public static BaseFile getFileFromPath(StringProperty propKey, JsonNode props) throws IOException {
        String val = propKey.apply(props);
        return getBaseFileFromPath(val);
    }

    /**
     * The system or "cluster" of nodes may share some kind of shared file system.  This could be KFS, HDFS, FTP, NFS, whatever.
     * The system is configured with a "default" system if you provide a file path that is relative wherever we want to construct
     * a base file.
     *
     * @param hdfsRootPath
     * @return
     */
    public static BaseFileSystem getGlobalNamespaceFileSystem(String hdfsRootPath) {
        if (FakeHDFS.apply() != null) {
            return new FileFileSystem(new File(FakeHDFS.apply(), hdfsRootPath));
        }
        return new DFSFileSystem(Fmt.S("/%s/%s", HDFSNameSpace.apply(), hdfsRootPath));
    }

    public static FileSystemCascadingContainer getNamespaceProviderContainer(String hdfsRootPath, File fileSystemRootPath) {
        BaseFileSystem providers[] = {getGlobalNamespaceFileSystem(hdfsRootPath), new FileFileSystem(fileSystemRootPath)};
        FileSystemCascadingContainer ioc = new FileSystemCascadingContainer(providers);
        return ioc;
    }

    public abstract boolean deleteFileSystem() throws IOException;

    public abstract F getFile(BaseFile af);

    public abstract F getFile(String path);

    public abstract F getFileEnsuringDir(String path);

    // *********** HELPER METHODS FOR GETTING FILESYSTEMS *****************

    public F getFileIfExists(String path) {
        F af = getFile(path);
        if (af != null) {
            if (af.exists()) {
                return af;
            }
        }
        return null;
    }

    public String getPathPart() {
        return pathPart;
    }

    public boolean isLocalFileSystem() {
        return true;
    }
}
