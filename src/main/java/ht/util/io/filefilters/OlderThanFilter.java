package ht.util.io.filefilters;

import java.io.File;
import java.io.FilenameFilter;

/**
 * Copyright (c) 2003 - present HiTorro All rights reserved. User: chris Date: Oct 31, 2006 Time: 11:07:23 PM
 */

public class OlderThanFilter implements FilenameFilter {
    private long m_millisOld;

    public OlderThanFilter(long olderThanMillis) {
        m_millisOld = olderThanMillis;
    }

    public boolean accept(File dir, String name) {
        long time = System.currentTimeMillis();
        return dir.lastModified() + m_millisOld < time;
    }
}