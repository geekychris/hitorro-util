package ht.util.io.filefilters;

import ht.util.core.string.StringUtil;
import ht.util.io.FileUtil;

import java.io.File;
import java.io.FilenameFilter;


/**
 * HTPredicate that looks for files containing the substring. Can compare the extensions ignoring the case if you so wish.
 *
 * @author chris
 */
public class FileNameContainsFilenameFilter implements FilenameFilter {
    private String m_subs;
    private boolean m_ignoreCase;

    /**
     * Constructor.
     *
     * @param subs
     * @param ignoreCase
     */
    public FileNameContainsFilenameFilter(String subs, boolean ignoreCase) {
        if (ignoreCase) {
            m_subs = subs.toLowerCase();
        } else {
            m_subs = subs;
        }
        m_ignoreCase = ignoreCase;

    }

    public boolean accept(File dir, String name) {
        String ext = FileUtil.getFileExtension(name);
        if (StringUtil.nullOrEmptyOrBlankString(ext)) {
            // not an interesting extension.
            return false;
        }
        if (m_ignoreCase) {
            ext = ext.toLowerCase();
        }
        return m_subs.contains(ext);
    }
}
