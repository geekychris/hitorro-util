package ht.util.io.filefilters;

import java.io.File;
import java.io.FilenameFilter;

public class StartsWithFilenameFilter implements FilenameFilter {
    private String m_pattern;

    public StartsWithFilenameFilter(String pattern) {
        m_pattern = pattern;
    }

    public boolean accept(File directory, String name) {
        if (name.startsWith(m_pattern)) {
            return true;
        }
        return false;
    }
}