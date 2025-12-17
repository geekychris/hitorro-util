package ht.util.io.filefilters;

import java.io.File;
import java.io.FilenameFilter;

/**
 * FilterEnum all files that are infact directories
 *
 * @author ccollins
 */
public class IsDirectoryFilenameFilter
        implements FilenameFilter {
    public IsDirectoryFilenameFilter() {
    }

    public boolean accept(File dir, String name) {
        File f = new File(dir, name);
        return f.isDirectory();
    }
}
