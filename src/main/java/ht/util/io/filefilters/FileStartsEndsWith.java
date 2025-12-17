package ht.util.io.filefilters;

import java.io.File;
import java.io.FilenameFilter;


/**
 * Look at the start or the end of a file to see if it matches our string.
 *
 * @author chris
 */
public class FileStartsEndsWith implements FilenameFilter {
    private String m_matchMe;
    private boolean m_ignoreCase;
    private boolean m_endsWith;

    /**
     * @param matchMe    string to test
     * @param ignoreCase true if we wish to ignore case
     * @param endsWith   false if we wish to test the start of the name true for the end of the name
     */
    public FileStartsEndsWith(String matchMe, boolean ignoreCase, boolean endsWith) {
        m_endsWith = endsWith;
        if (ignoreCase) {
            m_matchMe = matchMe.toLowerCase();
        } else {
            m_matchMe = matchMe;
        }
        m_ignoreCase = ignoreCase;

    }

    public boolean accept(File dir, String name) {
        if (m_ignoreCase) {
            name = name.toLowerCase();
        }
        if (m_endsWith) {
            return name.endsWith(m_matchMe);
        } else {
            return name.startsWith(m_matchMe);
        }
    }
}
