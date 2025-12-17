package ht.util.io.filedirwatch;

import java.io.File;
import java.io.IOException;

public interface DirWatcherTask {
    boolean execute(File f) throws IOException;
}
