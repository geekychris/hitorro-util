package ht.util.commandandcontrol;

import ht.jsontypesystem.JVS;
import ht.util.commandandcontrol.serialized.InfoRow;
import ht.util.io.StoreException;
import ht.util.typesystem.HTObjectInputStream;
import ht.util.typesystem.HTObjectOutputStream;
import ht.util.typesystem.HTSerializable;
import ht.util.typesystem.annotation.TypeClassMetaInfo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Copyright (c) 2003 - present HiTorro All rights reserved.
 * <p/>
 * User: chris
 */
@TypeClassMetaInfo(shortTypeName = "AR",
        isView = false,
        isPersisted = false,
        schemaVersion = ActionRequest.SerializationVersion)
public class ActionRequest implements HTSerializable {
    public static final int SerializationVersion = 1;
    private JVS args = new JVS();
    private String method;
    private List<InfoRow> rows = new ArrayList<InfoRow>();

    public List<InfoRow> getInfoRows() {
        return rows;
    }

    public void setInfoRows(List<InfoRow> list) {
        rows = list;
    }

    public void serialize(HTObjectOutputStream os) throws IOException, StoreException {
        os.writeInt(SerializationVersion);
        os.writeString(getMethod());
        os.writeString(getArgs().getStringRepresentation());
        os.writeInt(rows.size());
        for (InfoRow row : rows) {
            os.writeVersionedObject(row);
        }
    }

    public void deserialize(HTObjectInputStream os) throws IOException, ClassNotFoundException, StoreException {
        int version = os.readInt();
        setMethod(os.readString());
        int argSize = os.readInt();
        args = JVS.read(os.readString());
        int rowSize = os.readInt();
        for (int i = 0; i < rowSize; i++) {
            rows.add((InfoRow) os.readVersionedObject());
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

    public JVS getArgs() {
        return args;
    }

    public void setArgs(JVS args) {
        this.args = args;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }
}
