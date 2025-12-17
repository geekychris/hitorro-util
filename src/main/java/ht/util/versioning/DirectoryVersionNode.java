package ht.util.versioning;

import ht.jsontypesystem.JVS;

import java.io.File;

/**
 * Copyright (c) 2003 - present HiTorro All rights reserved. User: chris Date: Dec 5, 2006 Time: 4:08:19 PM
 */
public class DirectoryVersionNode extends VersionNode {
    protected File m_directory;
    private JVS m_manifest;

    public DirectoryVersionNode(JVS manifest, File directory, long major, long minor, long patch, long buildNumber) {
        super(major, minor, patch, buildNumber);
        m_directory = directory;
        m_manifest = manifest;
    }

    public DirectoryVersionNode() {

    }

    public DirectoryVersionNode(JVS manifest, File directory, String schemaVersion) {
        super(schemaVersion);
        m_directory = directory;
        m_manifest = manifest;
    }

    public File getDirectory() {
        return m_directory;
    }

    public void setDirectory(File dir) {
        m_directory = dir;
    }
}
