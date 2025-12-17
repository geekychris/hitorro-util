package ht.util.typesystem;

import ht.util.io.StoreException;

import java.io.IOException;

/**
 * Copyright (c) 2003 - present HiTorro All rights reserved. User: chris Date: Nov 28, 2006 Time: 8:35:09 AM
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
