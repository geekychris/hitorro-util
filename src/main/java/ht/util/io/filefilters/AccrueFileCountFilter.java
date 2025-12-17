package ht.util.io.filefilters;

import java.io.File;
import java.io.FileFilter;

public class AccrueFileCountFilter implements FileFilter {
    private int m_filesProcessed = 0;

    public AccrueFileCountFilter() {
    }

    public boolean accept(File file) {
        m_filesProcessed++;
        return false;
    }

    public int getFilesProcessed() {
        return m_filesProcessed;
    }
}
