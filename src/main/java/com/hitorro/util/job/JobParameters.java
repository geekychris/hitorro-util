package com.hitorro.util.job;

import com.hitorro.util.io.StoreException;
import com.hitorro.util.typesystem.BaseSession;
import com.hitorro.util.typesystem.HTObjectInputStream;
import com.hitorro.util.typesystem.HTObjectOutputStream;
import com.hitorro.util.typesystem.HTSerializable;

import java.io.IOException;

/**
 * Copyright (c) 2003 - present HiTorro All rights reserved. User: chris Date: Jan 25, 2005 Time: 6:06:16 PM
 */
public abstract class JobParameters implements HTSerializable {
    public static final int SerializationVersion = 1;
    // dms session used by object when editing
    private BaseSession _sessionForEditing;

    private String notifyGuid;
    private String notifyGuidState;

    public abstract String getJobName();

    public BaseSession getEditingSession() {
        return _sessionForEditing;
    }

    public void setEditingSession(BaseSession val) {
        _sessionForEditing = val;
    }

    public void serialize(HTObjectOutputStream os) throws IOException, StoreException {
        os.writeInt(JobParameters.SerializationVersion);
        os.writeString(notifyGuid);
        os.writeString(notifyGuidState);
    }

    public void deserialize(HTObjectInputStream os)
            throws IOException, ClassNotFoundException, StoreException {
        int version = os.readInt();

        switch (version) {
            case 1:
                notifyGuid = os.readString();
                notifyGuidState = os.readString();
        }

    }

    public String getNotifyGuid() {
        return notifyGuid;
    }

    public void setNotifyGuid(String notifyGuid) {
        this.notifyGuid = notifyGuid;
    }

    public String getNotifyGuidState() {
        return notifyGuidState;
    }

    public void setNotifyGuidState(String notifyGuidState) {
        this.notifyGuidState = notifyGuidState;
    }
}
