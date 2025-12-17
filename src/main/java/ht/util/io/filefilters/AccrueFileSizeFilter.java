package ht.util.io.filefilters;

import java.io.File;
import java.io.FileFilter;

/**
 * Using the FileFilter interface we are called by such things as the listFiles function of a directory to accumulate
 * file size stats.  We would use something like du but that only does blocks.
 */
public class AccrueFileSizeFilter implements FileFilter {
    private String m_fileExtensionToAccrue;
    private long m_size = 0;
    private int m_filesProcessed = 0;
    private int m_filesWithExtension = 0;
    private boolean m_acceptIfFileMatch = false;

    public AccrueFileSizeFilter(String fileExtensionToAccrue, boolean acceptIfFileMatch) {
        m_fileExtensionToAccrue = fileExtensionToAccrue;
        m_acceptIfFileMatch = acceptIfFileMatch;
    }

    public boolean accept(File file) {
        m_filesProcessed++;
        if (file.getName().endsWith(m_fileExtensionToAccrue)) {
            if (file.isFile()) {
                m_filesWithExtension++;
                m_size += file.length();
                if (m_acceptIfFileMatch) {
                    return true;
                }
            }
        }
        return false;
    }

    public long getFileSize() {
        return m_size;
    }

    public int getFilesProcessed() {
        return m_filesProcessed;
    }

    public int getFilesMatched() {
        return m_filesWithExtension;
    }
}