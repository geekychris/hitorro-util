package ht.util.io.resourcecache;

import ht.util.core.Env;
import ht.util.core.Platform;
import ht.util.io.FileUtil;
import ht.util.io.StoreException;
import ht.util.typesystem.HTObjectInputStream;
import ht.util.typesystem.HTObjectOutputStream;
import ht.util.typesystem.annotation.TypeClassMetaInfo;
import ht.util.versioning.DirectoryVersionNode;

import java.io.File;
import java.io.IOException;

/**
 * Copyright (c) 2003 - present HiTorro All rights reserved. User: chris Date: Dec 19, 2006 Time: 9:51:13 AM
 */

@TypeClassMetaInfo(shortTypeName = "ResourceVN",
        isView = false,
        isPersisted = false,
        schemaVersion = ResourceDirectoryVersionNode.SerializationVersion)
public class ResourceDirectoryVersionNode extends DirectoryVersionNode {
    public static final int SerializationVersion = 1;
    private static final String LinkFileName = ".requiresLink.txt";
    private String m_resource;
    private boolean m_requiresLink = false;
    private boolean m_currentLink = false;
    private File m_linkFile;
    private boolean m_isTemp = true;
    private int m_useCount = 0;
    private File m_currentLinkDir = null;
    private File m_versionedLinkDir = null;

    public ResourceDirectoryVersionNode() {

    }

    public ResourceDirectoryVersionNode(String resource, File directory, long major, long minor, long patch, long buildNumber) {
        super(null, directory, major, minor, patch, buildNumber);
        m_resource = resource;
        setLink(directory);
    }

    public ResourceDirectoryVersionNode(String resource, File directory, String schemaVersion) {
        super(null, directory, schemaVersion);
        m_resource = resource;
    }

    public void setTemp(boolean flag) {
        m_isTemp = false;
    }

    public void setDirectory(File dir) {
        super.setDirectory(dir);
        setLink(dir);
    }

    private void setLink(File directory) {
        m_linkFile = new File(directory, LinkFileName);
    }

    public int getUseCount() {
        return m_useCount;
    }

    public void incrementUseCount() {
        m_useCount++;
    }

    public void derementUseCount() {
        m_useCount--;
    }

    public void setOpenResourceVersionLink(boolean versioned, boolean current) throws Exception {
        m_requiresLink = versioned;
        this.m_currentLink = current;
        if (m_requiresLink) {
            //ensure link file exists.
            if (!m_linkFile.exists()) {
                FileUtil.writeLongValToFile(m_linkFile, System.currentTimeMillis());
            }
        } else {
            if (m_linkFile.exists()) {
                m_linkFile.delete();
            }
        }
        ensureLink();
    }

    public File getCurrentLink() {
        return m_currentLinkDir;
    }

    public File getVersionedLink() {
        return m_versionedLinkDir;
    }

    public void ensureLink() throws IOException {
        if (!m_isTemp) {
            File open = Env.getOpenResourceDir();

            //only if we are not temp do we
            if (m_currentLink) {
                if (m_currentLinkDir == null) {
                    // ensure we have the current link.
                    File current = new File(open, "current");
                    FileUtil.ensureDirectoryExists(current);

                    m_currentLinkDir = new File(current, this.m_resource);
                }
                m_currentLinkDir.delete();
                Platform.getPlatform().softLink(this.getDirectory(), m_currentLinkDir);
            }

            if (m_versionedLinkDir == null) {
                File versioned = new File(open, "versioned");
                FileUtil.ensureDirectoryExists(versioned);

                File versionResource = new File(versioned, this.m_resource);
                FileUtil.ensureDirectoryExists(versionResource);
                m_versionedLinkDir = new File(versionResource, getName());
            }
            m_versionedLinkDir.delete();
            if (this.m_requiresLink) {
                Platform.getPlatform().softLink(this.getDirectory(), m_versionedLinkDir);
            }

        }

    }

    public String getResource() {
        return m_resource;
    }

    public void serialize(HTObjectOutputStream os) throws IOException, StoreException {
        os.writeInt(SerializationVersion);
        super.serialize(os);
        os.writeString(m_resource);
        os.writeBoolean(m_requiresLink);
        os.writeBoolean(m_currentLink);
        if (m_linkFile != null) {
            os.writeString(m_linkFile.getAbsolutePath());
        } else {
            os.writeString(null);
        }

    }

    public void deserialize(HTObjectInputStream os) throws IOException, ClassNotFoundException, StoreException {
        int version = os.readInt();
        super.deserialize(os);
        m_resource = os.readString();
        m_requiresLink = os.readBoolean();
        m_currentLink = os.readBoolean();
        String s = os.readString();
        if (s != null) {
            m_linkFile = new File(s);
        }
    }

    public int getSerializationVersion() {
        return SerializationVersion;
    }

    public boolean isPersisted() {
        return false;
    }

    public boolean hasGuid() {
        return false;
    }

    public boolean hasSoftGuid() {
        return false;
    }
}
