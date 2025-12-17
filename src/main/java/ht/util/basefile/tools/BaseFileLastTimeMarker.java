package ht.util.basefile.tools;

import ht.util.basefile.fs.BaseFile;
import ht.util.core.Log;

import java.io.IOException;
import java.util.Date;

/**
 * Created by chris on 5/23/18.
 */
public class BaseFileLastTimeMarker {
    private BaseFile m_lastIndexTimeFile;

    private Date m_testDate;

    public BaseFileLastTimeMarker(BaseFile dir, String name) {

        m_lastIndexTimeFile = dir.getChild("%s.lasttimemarker", name);
    }

    public long getLastIndexTimeMillis() {
        m_testDate = new Date();
        if (m_lastIndexTimeFile.exists()) {
            return m_lastIndexTimeFile.getModifiedTime();
        } else {
            return -1000000000;
        }
    }

    public Date getLastIndexTime() {
        return new Date(getLastIndexTimeMillis());
    }

    public boolean set() {
        try {
            m_lastIndexTimeFile.writeLong(m_testDate.getTime());
        } catch (IOException e) {
            Log.util.error("%s %e", e, e);
        }
        return true;
    }
}