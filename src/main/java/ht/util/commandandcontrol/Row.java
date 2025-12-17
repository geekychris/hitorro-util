package ht.util.commandandcontrol;

import ht.util.commandandcontrol.serialized.InfoRow;
import ht.util.io.StoreException;
import ht.util.typesystem.HTObjectInputStream;
import ht.util.typesystem.HTObjectOutputStream;
import ht.util.typesystem.annotation.TypeClassMetaInfo;

import java.io.IOException;

/**
 * <p/>
 * Copyright (c) 2003 - present HiTorro All rights reserved. User: chris Date: Aug 20, 2005 Time: 3:52:11 PM
 */
@TypeClassMetaInfo(shortTypeName = "R",
        isView = false,
        isPersisted = false,
        schemaVersion = Row.SerializationVersion)
public class Row extends InfoRow {
    public static final int SerializationVersion = 1;

    private String names[];
    private Object[][] values;

    public boolean isInfoRow() {
        return false;
    }

    public String[] getNames() {
        return names;
    }

    public void setNames(String[] names) {
        this.names = names;
        values = new Object[names.length][];
    }

    public boolean isTuple(String name) {
        Object[] os = getValue(name);
        if (os == null || os.length == 0) {
            return false;
        }
        return os[0] instanceof ResponseTuple;
    }

    public String getZerothElement(String name) {
        Object[] os = getValue(name);
        if (os == null || os.length == 0) {
            return null;
        }
        return os[0].toString();
    }

    public Object[] getValue(String name) {
        for (int i = 0; i < names.length; i++) {
            if (names[i].equals(name)) {
                return values[i];
            }
        }
        return null;
    }

    /**
     * Simple
     *
     * @param row
     */
    public void setRow(Object row[]) {
        for (int i = 0; i < row.length; i++) {
            values[i] = new Object[1];
            values[i][0] = row[i];
        }
    }

    public void setFromTuples(Object o[][]) {
        values = o;
    }

    public void serialize(HTObjectOutputStream os) throws IOException, StoreException {
        os.writeInt(SerializationVersion);
        // number of names;
        os.writeInt(names.length);
        for (String name : names) {
            os.writeString(name);
        }
        for (int i = 0; i < values.length; i++) {
            boolean f = isTuple(i);
            os.writeBoolean(f);
            os.writeInt(values[i].length);
            if (f) {
                for (int j = 0; j < values[i].length; j++) {
                    ResponseTuple tr = (ResponseTuple) values[i][j];
                    os.writeVersionedObject(tr);
                }
            } else {
                for (int j = 0; j < values[i].length; j++) {
                    os.writeString(values[i][j].toString());
                }
            }

        }

    }

    public void deserialize(HTObjectInputStream os) throws IOException, ClassNotFoundException, StoreException {
        int version = os.readInt();
        int size = os.readInt();
        names = new String[size];
        values = new Object[size][];
        for (int i = 0; i < size; i++) {
            names[i] = os.readString();
        }

        for (int i = 0; i < size; i++) {
            boolean flag = os.readBoolean();
            int rowSize = os.readInt();
            values[i] = new Object[rowSize];
            for (int j = 0; j < rowSize; j++) {
                if (flag) {
                    values[i][j] = os.readVersionedObject();
                } else {
                    values[i][j] = os.readString();
                }
            }
        }
    }

    private boolean isTuple(int index) {
        if (values[index] != null && values[index].length > 0) {
            return values[index][0] instanceof ResponseTuple;
        }
        return false;
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


