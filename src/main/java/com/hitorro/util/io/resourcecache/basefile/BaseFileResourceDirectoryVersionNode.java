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
package com.hitorro.util.io.resourcecache.basefile;

import com.hitorro.util.basefile.fs.BaseFile;
import com.hitorro.util.basefile.fs.BaseFileSystem;
import com.hitorro.util.io.StoreException;
import com.hitorro.util.io.resourcecache.ResourceDirectoryVersionNode;
import com.hitorro.util.typesystem.HTObjectInputStream;
import com.hitorro.util.typesystem.HTObjectOutputStream;
import com.hitorro.util.typesystem.annotation.TypeClassMetaInfo;
import com.hitorro.util.versioning.BaseFileDirectoryVersionNode;

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
    private boolean requiresLink = false;
    private boolean currentLink = false;
    private BaseFile linkFile;
    private boolean isTemp = true;
    private int useCount = 0;
    private BaseFile currentLinkDir = null;
    private BaseFile versionedLinkDir = null;

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
        isTemp = false;
    }

    public void setOpenResourceVersionLink(boolean versioned, boolean current) throws IOException {
        requiresLink = versioned;
        this.currentLink = current;
        if (requiresLink) {
            //ensure link file exists.
            if (!linkFile.exists()) {
                linkFile.writeString(Long.toString(System.currentTimeMillis()));
            }
        } else {
            if (linkFile.exists()) {
                linkFile.delete();
            }
        }
        ensureLink();
    }

    public BaseFile getCurrentLink() {
        return currentLinkDir;
    }

    public BaseFile getVersionedLink() {
        return versionedLinkDir;
    }

    public void ensureLink() throws IOException {
        if (!isTemp) {
            BaseFile open = cache.getOpenResourceDir();

            //only if we are not temp do we
            if (currentLink) {
                if (currentLinkDir == null) {
                    // ensure we have the current link.
                    BaseFile current = open.getChild("current");
                    current.mkdir();

                    currentLinkDir = current.getChild(this.m_resource);
                }
                currentLinkDir.delete();
                if (this.getDirectory().supportsSoftLink()) {
                    this.getDirectory().linkTo(currentLinkDir);
                }

            }

            if (versionedLinkDir == null) {
                BaseFile versioned = open.getChild("versioned");
                versioned.mkdir();

                BaseFile versionResource = versioned.getChild(this.m_resource);
                versionResource.mkdir();
                versionedLinkDir = versionResource.getChild(getName());
            }
            versionedLinkDir.delete();
            if (this.requiresLink) {
                if (this.getDirectory().supportsSoftLink()) {
                    this.getDirectory().linkTo(versionedLinkDir);
                }
            }

        }

    }

    public void setDirectory(BaseFile dir) {
        super.setDirectory(dir);
        setLink(dir);
    }

    private void setLink(BaseFile directory) {
        linkFile = directory.getChild(LinkFileName);
    }

    public int getUseCount() {
        return useCount;
    }

    public void incrementUseCount() {
        useCount++;
    }

    public void derementUseCount() {
        useCount--;
    }

    public String getResource() {
        return m_resource;
    }

    public void serialize(HTObjectOutputStream os) throws IOException, StoreException {
        os.writeInt(SerializationVersion);
        super.serialize(os);
        os.writeString(m_resource);
        os.writeBoolean(requiresLink);
        os.writeBoolean(currentLink);
        if (linkFile != null) {
            os.writeString(linkFile.getAbsolutePath());
        } else {
            os.writeString(null);
        }

    }

    public void deserialize(HTObjectInputStream os) throws IOException, ClassNotFoundException, StoreException {
        int version = os.readInt();
        super.deserialize(os);
        m_resource = os.readString();
        requiresLink = os.readBoolean();
        currentLink = os.readBoolean();
        String s = os.readString();
        if (s != null) {
            linkFile = BaseFileSystem.getBaseFileFromPath(s);
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
