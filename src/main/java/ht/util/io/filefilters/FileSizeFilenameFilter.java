package ht.util.io.filefilters;

import java.io.File;
import java.io.FilenameFilter;


/**
 * HTPredicate that looks for files with a similar extension. Can compare the extensions ignoring the case if you so wish.
 * <p/>
 * we either look for files that are greater than the size we set, or less than or equal.
 *
 * @author chris
 */
public class FileSizeFilenameFilter implements FilenameFilter {
    private long m_size;
    private boolean m_greaterThan;

    /**
     * Consider a file greater than or smaller than the current size.
     *
     * @param size
     * @param greaterThan
     */
    public FileSizeFilenameFilter(long size, boolean greaterThan) {
        m_size = size;
        m_greaterThan = greaterThan;
    }

    public boolean accept(File dir, String name) {
        File f = new File(dir, name);
        if (m_greaterThan) {
            return f.length() > m_size;
        }
        return f.length() <= m_size;
    }
}

