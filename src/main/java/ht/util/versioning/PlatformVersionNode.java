package ht.util.versioning;

import ht.jsontypesystem.JVS;
import ht.util.core.Log;
import ht.util.io.FileUtil;
import ht.util.json.keys.propaccess.PropaccessError;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Copyright (c) 2003 - present HiTorro All rights reserved. User: chris Date: Dec 5, 2006 Time: 6:36:11 PM
 * <p/>
 * Definition of a platform binary release and a listFiles of the server configurations that use it.
 */
public class PlatformVersionNode<T extends ConfigTag> extends DirectoryVersionNode {
    private int m_usageCount = 0;
    private List<T> m_serverConfigs = new ArrayList<T>();

    public PlatformVersionNode(JVS manifest,
                               File directory) throws PropaccessError {
        super(manifest, directory,
                manifest.getLong("major"),
                manifest.getLong("minor"),
                manifest.getLong("patch"),
                manifest.getLong("number"));
    }

    public PlatformVersionNode(File directory) {
        super(new JVS(), directory, 99, 99, 99, 99);
    }

    public void addDependentServer(T sc) {
        m_serverConfigs.add(sc);
    }

    public List<T> getConfigs() {
        return m_serverConfigs;
    }

    public void removeConfig(T sc) {
        m_serverConfigs.remove(sc);
    }

    public int getUsageCount() {
        return m_usageCount;
    }

    public boolean delete() {
        int count = m_serverConfigs.size();
        if (count > 0) {
            Log.util.error("Attempt to delete platform config %s failed as it is used by %s servers", this.getVersion(), count);
            return false;
        }
        FileUtil.deleteDirectoryContent(m_directory, true);
        return true;
    }
}
