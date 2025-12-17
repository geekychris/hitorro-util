package ht.util.io;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Wrapper around a file that keeps track of a couple basic stats such as modified time and size.  You can query to see
 * if this has changed.  Can report when files change in a set.  Will reset what was changed to the new values
 */
public class FileChangeContainer {
    private File file;

    private long modTime;
    private long tempModTime;
    private long size;
    private long tempSize;

    public FileChangeContainer(File file) {
        this.file = file;
        getTS();
        applyChanges();
    }

    public static List<FileChangeContainer> getChangesFromFileList(List<File> list) {
        List<FileChangeContainer> cont = new ArrayList();
        for (File f : list) {
            cont.add(new FileChangeContainer(f));
        }
        return cont;
    }

    private void getTS() {
        tempModTime = file.lastModified();
        tempSize = file.length();
    }

    public boolean hasChanged() {
        getTS();
        return isDifferent();
    }

    public boolean hasChangedReset() {
        if (hasChanged()) {
            applyChanges();
            return true;
        }
        return false;
    }

    private boolean isDifferent() {
        if (tempModTime != modTime) {
            return true;
        }
        if (tempSize != size) {
            return true;
        }
        return false;
    }

    private void applyChanges() {
        modTime = tempModTime;
        size = tempSize;
    }
}
