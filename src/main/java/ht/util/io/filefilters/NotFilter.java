package ht.util.io.filefilters;

import java.io.File;
import java.io.FilenameFilter;

/**
 * Copyright (c) 2003 - present HiTorro All rights reserved. User: chris Date: Dec 15, 2006 Time: 8:19:49 PM
 */
public class NotFilter implements FilenameFilter {
    private FilenameFilter m_filter;

    public NotFilter(FilenameFilter filter) {
        m_filter = filter;
    }

    public boolean accept(File file, String string) {
        return !m_filter.accept(file, string);
    }
}
