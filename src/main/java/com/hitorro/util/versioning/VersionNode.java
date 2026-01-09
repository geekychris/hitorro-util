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
package com.hitorro.util.versioning;

import com.hitorro.util.core.string.Fmt;
import com.hitorro.util.core.string.StringUtil;
import com.hitorro.util.io.StoreException;
import com.hitorro.util.typesystem.HTObjectInputStream;
import com.hitorro.util.typesystem.HTObjectOutputStream;
import com.hitorro.util.typesystem.HTSerializable;
import com.hitorro.util.typesystem.annotation.TypeClassMetaInfo;

import java.io.IOException;

/**
 */
@TypeClassMetaInfo(shortTypeName = "VN",
        isView = false,
        isPersisted = false,
        schemaVersion = VersionNode.SerializationVersion)
public class VersionNode implements HTSerializable, Comparable<VersionNode> {
    public static final int SerializationVersion = 1;
    private long m_major;
    private long m_minor;
    private long m_patch;
    private long buildNumber;
    private long[] m_version = new long[4];
    private String stringName;

    public VersionNode() {

    }

    public VersionNode(long major, long minor, long patch, long buildNumber) {
        init(major, minor, patch, buildNumber);
    }

    public VersionNode(String schemaVersion) {
        String parts[] = StringUtil.tokenizeFromSingleChar(schemaVersion, ".");
        init(Long.parseLong(parts[0]),
                Long.parseLong(parts[1]),
                Long.parseLong(parts[2]),
                Long.parseLong(parts[3]));
    }

    public boolean equals(Object o) {
        if (o instanceof VersionNode) {
            return ((VersionNode) o).stringName.equals(stringName);
        }
        return false;
    }

    public int hashCode() {
        return stringName.hashCode();
    }

    public void init(long major, long minor, long patch, long buildNumber) {
        m_major = major;
        m_minor = minor;
        m_patch = patch;
        this.buildNumber = buildNumber;
        initAux();
        stringName = Fmt.S("%s.%s.%s.%s", Long.toString(major), Long.toString(minor), Long.toString(patch), Long.toString(buildNumber));
    }

    private void initAux() {
        m_version[0] = m_major;
        m_version[1] = m_minor;
        m_version[2] = m_patch;
        m_version[3] = buildNumber;
    }

    public String toString() {
        return stringName;
    }

    public String getName() {
        return stringName;
    }

    public long getMajor() {
        return m_major;
    }

    public long getMinor() {
        return m_minor;
    }

    public long getPatch() {
        return m_patch;
    }

    public long getBuildNumber() {
        return buildNumber;
    }

    public String getVersion() {
        return Fmt.S("%s.%s.%s.%s", m_major, m_minor, m_patch, buildNumber);
    }

    public boolean meetsVersionCriteria(VersionPartComparitor[] comp) {
        for (int i = 0; i < 4; i++) {
            if (!comp[i].match(m_version[i])) {
                return false;
            }
        }
        return true;
    }

    public void serialize(HTObjectOutputStream os) throws IOException, StoreException {
        os.writeInt(SerializationVersion);
        os.writeLong(m_major);
        os.writeLong(m_minor);
        os.writeLong(m_patch);
        os.writeLong(buildNumber);
        os.writeString(stringName);
    }

    public void deserialize(HTObjectInputStream os) throws IOException, ClassNotFoundException, StoreException {
        int version = os.readInt();

        m_major = os.readLong();
        m_minor = os.readLong();
        m_patch = os.readLong();
        buildNumber = os.readLong();
        stringName = os.readString();
        initAux();
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

    public int compareTo(VersionNode o) {
        for (int i = 0; i < m_version.length; i++) {
            long a = m_version[i];
            long b = o.m_version[i];
            if (a == b) {
                continue;
            }
            if (a > b) {
                return 1;
            }
            if (a < b) {
                return -1;
            }
        }
        return 0;
    }
}
