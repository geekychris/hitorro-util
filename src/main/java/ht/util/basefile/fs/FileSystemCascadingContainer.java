package ht.util.basefile.fs;

import ht.util.core.Console;

/**
 * Cascading set of file systems.  Used to mask where data comes from on a read operation.  One can imagine that you
 * have a "fileSystem" on file and one locally, or maybe on a master disk and one on a secondary.  Or you are using them
 * as configuration overides, one in HOME, one in BIN. This mechanism allows you to get a file from the first place it
 * can find it in a specified seek order.
 * <p/>
 * User: chris
 */
public class FileSystemCascadingContainer {
    private BaseFileSystem fileSystems[];

    public FileSystemCascadingContainer(BaseFileSystem providers[]) {
        this.fileSystems = providers;
    }

    public BaseFile getFileIfExists(String path, boolean supressCascade) {
        if (supressCascade) {
            BaseFile bf = fileSystems[fileSystems.length - 1].getFileIfExists(path);
            if (bf != null) {
                return bf;
            }
        }
        for (BaseFileSystem p : fileSystems) {
            BaseFile bf = p.getFileIfExists(path);
            if (bf != null) {
                return bf;
            }
        }
        return null;
    }

    public BaseFile getFileForWrite(String path) {
        return fileSystems[0].getFileEnsuringDir(path);
    }

    /**
     * Given a file in one tier, will provide the file in the other tier.  This is effectively "give me the relative
     * path of <fs1:path> as <fs2:path>.
     * <p/>
     * This implementation is somewhat naive and assumes a hdfs and a local file system
     *
     * @param bf
     * @return
     */
    public BaseFile getPeerFile(BaseFile bf) {
        int i = 0;
        if (fileSystems.length == 2) {
            if (fileSystems[i].isLocalFileSystem() == bf.isLocal()) {
                i = 1;
            }
            return fileSystems[i].getFile(bf.path);
        }
        return null;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (fileSystems != null) {
            for (BaseFileSystem p : fileSystems) {
                Console.bprintln(sb, p.toString());
            }
            return sb.toString();
        }
        return "Not set";
    }

}
