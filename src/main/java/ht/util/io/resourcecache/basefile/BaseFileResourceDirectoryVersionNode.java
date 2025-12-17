/*
 * Copyright (c) 2006-2025 Chris Collins
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package ht.util.io.resourcecache.basefile;

import ht.util.basefile.fs.BaseFile;
import ht.util.basefile.fs.BaseFileSystem;
import ht.util.io.StoreException;
import ht.util.io.resourcecache.ResourceDirectoryVersionNode;
import ht.util.typesystem.HTObjectInputStream;
import ht.util.typesystem.HTObjectOutputStream;
import ht.util.typesystem.annotation.TypeClassMetaInfo;
import ht.util.versioning.BaseFileDirectoryVersionNode;

import java.io.IOException;

@TypeClassMetaInfo(shortTypeName = "BaseFileResourceVN",
        isView = false,
        isPersisted = false,
        schemaVersion = ResourceDirectoryVersionNode.SerializationVersion)
public class BaseFileResourceDirectoryVersionNode extends BaseFileDirectoryVersionNode {
    public static final int SerializationVersion = 1;
    private static final String LinkFileName = ".requiresLink.txt";
    private BaseFileResourceCache cache;
    private String m_resource;
    private boolean m_requiresLink = false;
    private boolean m_currentLink = false;
    private BaseFile m_linkFile;
    private boolean m_isTemp = true;
    private int m_useCount = 0;
    private BaseFile m_currentLinkDir = null;
    private BaseFile m_versionedLinkDir = null;

    public BaseFileResourceDirectoryVersionNode() {

    }

    public BaseFileResourceDirectoryVersionNode(BaseFileResourceCache cache, String resource, BaseFile directory, long major, long minor, long patch, long buildNumber) {
        super(null, directory, major, minor, patch, buildNumber);
        this.cache = cache;
        m_resource = resource;
        setLink(directory);
    }

    public BaseFileResourceDirectoryVersionNode(String resource, BaseFile directory, String schemaVersion) {
        super(null, directory, schemaVersion);
        m_resource = resource;
    }

    public void setTemp(boolean flag) {
        m_isTemp = false;
    }

    public void setOpenResourceVersionLink(boolean versioned, boolean current) throws IOException {
        m_requiresLink = versioned;
        this.m_currentLink = current;
        if (m_requiresLink) {
            //ensure link file exists.
            if (!m_linkFile.exists()) {
                m_linkFile.writeString(Long.toString(System.currentTimeMillis()));
            }
        } else {
            if (m_linkFile.exists()) {
                m_linkFile.delete();
            }
        }
        ensureLink();
    }

    public BaseFile getCurrentLink() {
        return m_currentLinkDir;
    }

    public BaseFile getVersionedLink() {
        return m_versionedLinkDir;
    }

    public void ensureLink() throws IOException {
        if (!m_isTemp) {
            BaseFile open = cache.getOpenResourceDir();

            //only if we are not temp do we
            if (m_currentLink) {
                if (m_currentLinkDir == null) {
                    // ensure we have the current link.
                    BaseFile current = open.getChild("current");
                    current.mkdir();

                    m_currentLinkDir = current.getChild(this.m_resource);
                }
                m_currentLinkDir.delete();
                if (this.getDirectory().supportsSoftLink()) {
                    this.getDirectory().linkTo(m_currentLinkDir);
                }

            }

            if (m_versionedLinkDir == null) {
                BaseFile versioned = open.getChild("versioned");
                versioned.mkdir();

                BaseFile versionResource = versioned.getChild(this.m_resource);
                versionResource.mkdir();
                m_versionedLinkDir = versionResource.getChild(getName());
            }
            m_versionedLinkDir.delete();
            if (this.m_requiresLink) {
                if (this.getDirectory().supportsSoftLink()) {
                    this.getDirectory().linkTo(m_versionedLinkDir);
                }
            }

        }

    }

    public void setDirectory(BaseFile dir) {
        super.setDirectory(dir);
        setLink(dir);
    }

    private void setLink(BaseFile directory) {
        m_linkFile = directory.getChild(LinkFileName);
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
            m_linkFile = BaseFileSystem.getBaseFileFromPath(s);
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
