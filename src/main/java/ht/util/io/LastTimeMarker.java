package ht.util.io;

import ht.util.core.Log;
import ht.util.core.string.Fmt;

import java.io.File;
import java.io.IOException;
import java.util.Date;

/**
 * Copyright (c) 2003 - present HiTorro All rights reserved. User: chris Date: Feb 23, 2005 Time: 10:31:11 AM
 * <p/>
 * Manage a file based time marker.  Provides a persisted way to track the last time something was done. Once the last
 * time is returned it sets in memory the time that will be committed when set is called.
 */
public class LastTimeMarker {
    private File m_lastIndexTimeFile;
    private Date m_testDate;

    public LastTimeMarker(File dir, String name) {

        m_lastIndexTimeFile = new File(dir, Fmt.S("%s.lasttimemarker", name));
    }

    public long getLastIndexTimeMillis() {
        m_testDate = new Date();
        if (m_lastIndexTimeFile.exists()) {
            return m_lastIndexTimeFile.lastModified();
        } else {
            return -1000000000;
        }

    }

    public Date getLastIndexTime() {
        return new Date(getLastIndexTimeMillis());
    }

    public boolean set() {
        try {
            FileUtil.writeLongValToFile(m_lastIndexTimeFile, m_testDate.getTime());
        } catch (IOException e) {
            Log.util.error("%s %e", e, e);
        }
        return true;
    }
}
