package ht.util.io.filefilters;

import ht.util.core.string.StringUtil;
import ht.util.io.FileUtil;

import java.io.File;
import java.io.FilenameFilter;


/**
 * HTPredicate that looks for files with a similar extension. Can compare the extensions ignoring the case if you so wish.
 *
 * @author ccollins
 */
public class FilenameExtensionFilter implements FilenameFilter {

    public static final FilenameExtensionFilter Jar = new FilenameExtensionFilter("jar", true);
    public static final FilenameExtensionFilter Zip = new FilenameExtensionFilter("zip", true);
    public static final FilenameExtensionFilter Sh = new FilenameExtensionFilter("sh", true);

    public static final OrCollection JarOrZip = new OrCollection(Jar, Zip);

    private String m_extension;
    private boolean m_ignoreCase;

    /**
     * Constructor.
     *
     * @param extension  to look for
     * @param ignoreCase if true do a case insensative search
     */
    public FilenameExtensionFilter(String extension, boolean ignoreCase) {
        if (ignoreCase) {
            m_extension = extension.toLowerCase();
        } else {
            m_extension = extension;
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
        return m_extension.equals(ext);
    }
}
