package ht.util.basefile.fs.zk;

import ht.util.basefile.fs.BaseFile;
import ht.util.basefile.fs.BaseFileSystem;
import ht.util.core.HTException;
import ht.util.zookeeper.ZKContext;

public class ZKFileSystem extends BaseFileSystem<ZKFile, ZKFileSystem> {
    ZKContext getCoordinator() {
        return ZKContext.me;
    }

    @Override
    public boolean deleteFileSystem() {
        return false;
    }

    @Override
    public ZKFile getFile(final BaseFile af) {
        if (af instanceof ZKFile) {
            return (ZKFile) af;
        }
        return null;
    }

    @Override
    public ZKFile getFileEnsuringDir(final String path) {
        ZKFile f = getFile(path);
        if (f != null) {
            f.mkdir();
        }
        return f;
    }

    @Override
    public ZKFile getFile(String path) {
        try {
            return new ZKFile(this, path);
        } catch (HTException e) {
            return null;
        }
    }


}