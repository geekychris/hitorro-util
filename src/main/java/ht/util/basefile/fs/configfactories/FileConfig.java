package ht.util.basefile.fs.configfactories;

import ht.util.basefile.fs.file.FileFileSystem;

/**
 *
 */
public class FileConfig extends FileSystemConfig {
    String path;

    public String getPath() {
        return path;
    }

    public FileFileSystem getFileSystem() {
        return new FileFileSystem(this);
    }
}
