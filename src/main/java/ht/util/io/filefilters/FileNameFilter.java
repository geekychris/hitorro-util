package ht.util.io.filefilters;

import java.io.File;
import java.io.FilenameFilter;

/**
 * Match filenames that have an exact char test....not a very usefull class if you ask me.
 *
 * @author chris
 */
public class FileNameFilter implements FilenameFilter {
    private String m_matchMe;
    private boolean m_ignoreCase;

    /**
     * @param matchMe    string to test whole filename to
     * @param ignoreCase true if we wish to ignore case
     */
    public FileNameFilter(String matchMe, boolean ignoreCase) {
        m_matchMe = matchMe;
        this.m_ignoreCase = ignoreCase;
    }

    public boolean accept(File dir, String name) {
        if (m_ignoreCase) {
            return name.equalsIgnoreCase(m_matchMe);
        }
        return name.equals(m_matchMe);
    }
}

