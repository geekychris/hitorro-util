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
package com.hitorro.util.typesystem;

import com.hitorro.util.io.StoreException;

import java.io.IOException;

/**
 */
public abstract class VersionBaseType<T extends BaseSession> extends BaseType<T> {
    private transient boolean m_markedFlush = false;

    private transient int m_versionStampSnapshot;

    private int m_versionStamp;

    public int getVersionStamp() {
        return m_versionStamp;
    }

    public void setVersionStamp(int version) {
        m_versionStamp = version;
    }


    public void snapshotVersionStamp() {
        m_versionStampSnapshot = m_versionStamp;
    }

    public void recoverSnapshotVersionStamp() {
        m_versionStamp = m_versionStampSnapshot;
    }

    public void markFlushed() {
        m_markedFlush = true;
    }

    public boolean isMarkedFlushed() {
        return m_markedFlush;
    }

    public void resetMarkedFlushed() {
        m_markedFlush = false;
    }

    public void serialize(HTObjectOutputStream os)
            throws IOException, StoreException {
        super.serialize(os);
        os.writeInt(m_versionStamp);
    }

    public void deserialize(HTObjectInputStream os)
            throws IOException, ClassNotFoundException, StoreException {
        int version = os.readInt();
        super.deserialize(os);
        switch (version) {
            case 1:
                m_versionStamp = os.readInt();
        }
    }

    public String getGuid() {
        return null;
    }

    public void setGuid(String guid) {

    }
}
