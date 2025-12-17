package ht.util.commandandcontrol;

import ht.util.commandandcontrol.serialized.InfoRow;
import ht.util.io.StoreException;
import ht.util.typesystem.HTObjectInputStream;
import ht.util.typesystem.HTObjectOutputStream;
import ht.util.typesystem.annotation.TypeClassMetaInfo;

import java.io.IOException;

/**
 * <p/>
 * Copyright (c) 2003 - present HiTorro All rights reserved. User: chris Date: Aug 20, 2005 Time: 3:52:38 PM
 */
@TypeClassMetaInfo(shortTypeName = "RT",
        isView = false,
        isPersisted = false,
        schemaVersion = ResponseTuple.SerializationVersion)
public class ResponseTuple extends InfoRow {
    public static final int SerializationVersion = 1;
    private String name;
    private String[] names;
    private String[] values;

    public String getValue(String name) {
        for (int i = 0; i < names.length; i++) {
            if (names[i].equals(name)) {
                return values[i];
            }
        }
        return null;
    }

    public String getTupleName() {
        return name;
    }

    public void setTupleName(String n) {
        name = n;
    }

    public boolean isInfoRow() {
        return false;
    }

    public String[] getNames() {
        return names;
    }

    public void setNames(String[] names) {
        this.names = names;
    }

    public String[] getValues() {
        return values;
    }

    public void setValues(String[] values) {
        this.values = values;
    }

    public void serialize(HTObjectOutputStream os) throws IOException, StoreException {
        os.writeInt(SerializationVersion);
        os.writeInt(names.length);
        os.writeString(name);
        for (String name : names) {
            os.writeString(name);
        }
        os.writeInt(values.length);
        for (String value : values) {
            os.writeString(value);
        }
    }

    public void deserialize(HTObjectInputStream os) throws IOException, ClassNotFoundException, StoreException {
        int version = os.readInt();
        int size = os.readInt();
        name = os.readString();
        names = new String[size];
        for (int i = 0; i < size; i++) {
            names[i] = os.readString();
        }

        size = os.readInt();
        values = new String[size];
        for (int i = 0; i < size; i++) {
            values[i] = os.readString();
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


