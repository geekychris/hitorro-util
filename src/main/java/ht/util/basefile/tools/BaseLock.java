package ht.util.basefile.tools;

import ht.util.basefile.fs.BaseFile;

import java.io.IOException;
import java.io.PrintWriter;

/**
 *
 */
public class BaseLock {
    private BaseFile bf;

    private String id;
    private String name;
    private BaseFile lockFile;
    private PrintWriter pw;

    public BaseLock(BaseFile bf, String name, String id) {
        this.bf = bf;
        this.id = id;
        this.name = name;
        this.lockFile = bf.getChild(name);
    }

    /**
     * @return
     */
    public boolean takeLock() throws IOException {
        if (!bf.mkParentDir()) {
            return false;
        }
        pw = lockFile.getPrintWriter();
        return pw != null;
    }

    public long getLockAge() {
        return lockFile.getModifiedTime();
    }

    public void touch() throws IOException {
        lockFile.setLastModified(System.currentTimeMillis());
    }

    public void release() throws IOException {
        if (pw == null) {
            return;
        }
        pw.close();
        lockFile.delete();
    }
}
