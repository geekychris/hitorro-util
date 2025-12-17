package ht.util.io.filedirwatch;

import java.io.File;

public class DeleteTask implements DirWatcherTask {
    public boolean execute(File f) {
        if (f != null) {
            if (f.exists()) {
                f.delete();
            }
        }

        return true;
    }
}
