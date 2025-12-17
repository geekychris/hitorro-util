package ht.util.basefile.fs.jarfile;

import ht.util.basefile.fs.BaseFile;
import ht.util.basefile.fs.BaseFileSystem;

/**
 *
 */
public class JarFileSystem extends BaseFileSystem<JarFileFile, JarFileSystem> {

    @Override
    public boolean deleteFileSystem() {
        return false;
    }

    @Override
    public JarFileFile getFile(final BaseFile af) {
        return null;
    }

    @Override
    public JarFileFile getFile(final String path) {
        return null;
    }

    @Override
    public JarFileFile getFileEnsuringDir(final String path) {
        return null;
    }
}

