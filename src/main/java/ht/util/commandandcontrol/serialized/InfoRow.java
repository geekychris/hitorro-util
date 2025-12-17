package ht.util.commandandcontrol.serialized;

import ht.util.io.StoreException;
import ht.util.typesystem.HTObjectInputStream;
import ht.util.typesystem.HTObjectOutputStream;
import ht.util.typesystem.HTSerializable;
import ht.util.typesystem.annotation.TypeClassMetaInfo;

import java.io.IOException;

/**
 * Copyright (c) 2003 - present HiTorro All rights reserved.
 * <p/>
 * User: chris
 */
@TypeClassMetaInfo(shortTypeName = "IR",
        isView = false,
        isPersisted = false,
        schemaVersion = InfoRow.SerializationVersion)
public class InfoRow implements HTSerializable {
    public static final int SerializationVersion = 1;
    private String level;
    private String message;

    public InfoRow() {

    }

    public InfoRow(String level, String message) {
        setLevel(level);
        setMessage(message);
    }

    public boolean isInfoRow() {
        return true;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void serialize(HTObjectOutputStream os) throws IOException, StoreException {
        os.writeInt(SerializationVersion);
        os.writeString(level);
        os.writeString(message);
    }

    public void deserialize(HTObjectInputStream os) throws IOException, ClassNotFoundException, StoreException {
        int version = os.readInt();
        level = os.readString();
        message = os.readString();
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
