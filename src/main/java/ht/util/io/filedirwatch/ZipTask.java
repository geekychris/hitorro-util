package ht.util.io.filedirwatch;

import ht.util.core.string.Fmt;
import ht.util.io.FileUtil;

import java.io.File;
import java.io.IOException;

/**
 * Compress the fle and possibly delete the original file afterwards.
 */
public class ZipTask implements DirWatcherTask {
    private boolean deleteAfter;

    public ZipTask(boolean deletePostZip) {
        deleteAfter = deletePostZip;
    }

    public boolean execute(File f) throws IOException {
        File zip = new File(Fmt.S("%s.gz", f.getAbsoluteFile()));
        FileUtil.gzipFile(f, zip, false);
        if (deleteAfter) {
            f.delete();
        }
        return true;
    }
}