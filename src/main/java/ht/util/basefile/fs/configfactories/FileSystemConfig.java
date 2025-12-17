package ht.util.basefile.fs.configfactories;

import ht.util.basefile.fs.BaseFileSystem;

import java.io.IOException;

/**
 *
 */
public abstract class FileSystemConfig<S extends BaseFileSystem> {
    public abstract S getFileSystem() throws IOException;
}
