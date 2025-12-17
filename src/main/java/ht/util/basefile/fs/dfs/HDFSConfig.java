package ht.util.basefile.fs.dfs;

import ht.util.basefile.fs.BaseFileSystem;
import ht.util.basefile.fs.configfactories.FileSystemConfig;

/**
 *
 */
public class HDFSConfig extends FileSystemConfig {
    String hdfsURI;
    String rootPath;

    public BaseFileSystem getFileSystem() {
        return new DFSFileSystem(this);
    }

    public String getHdfsURI() {
        return hdfsURI;
    }

    public String getRootPath() {
        return rootPath;
    }
}
