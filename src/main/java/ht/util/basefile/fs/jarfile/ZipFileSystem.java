package ht.util.basefile.fs.jarfile;

import ht.util.basefile.fs.BaseFile;
import ht.util.basefile.fs.BaseFileSystem;

/**
 *
 */
public class ZipFileSystem extends BaseFileSystem<ZipFileFile, ZipFileSystem> {

    @Override
    public boolean deleteFileSystem() {
        return false;
    }

    @Override
    public ZipFileFile getFile(final BaseFile af) {
        return null;
    }

    @Override
    public ZipFileFile getFile(final String path) {
        return null;
    }

    @Override
    public ZipFileFile getFileEnsuringDir(final String path) {
        return null;
    }
}

