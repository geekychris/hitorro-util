package ht.util.versioning;

import ht.util.basefile.fs.BaseFile;

import java.util.Map;

/**
 *
 */
public class BaseFileDirectoryVersionNode extends VersionNode {
    protected BaseFile m_directory;
    private Map<String, String> m_manifest;

    public BaseFileDirectoryVersionNode(Map<String, String> manifest, BaseFile directory, long major, long minor, long patch, long buildNumber) {
        super(major, minor, patch, buildNumber);
        m_directory = directory;
        m_manifest = manifest;
    }

    public BaseFileDirectoryVersionNode() {

    }

    public BaseFileDirectoryVersionNode(Map<String, String> manifest, BaseFile directory, String schemaVersion) {
        super(schemaVersion);
        m_directory = directory;
        m_manifest = manifest;
    }

    public BaseFile getDirectory() {
        return m_directory;
    }

    public void setDirectory(BaseFile dir) {
        m_directory = dir;
    }
}
