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
package com.hitorro.util.commandandcontrol;

import com.hitorro.jsontypesystem.JVS;
import com.hitorro.util.commandandcontrol.serialized.InfoRow;
import com.hitorro.util.io.StoreException;
import com.hitorro.util.typesystem.HTObjectInputStream;
import com.hitorro.util.typesystem.HTObjectOutputStream;
import com.hitorro.util.typesystem.HTSerializable;
import com.hitorro.util.typesystem.annotation.TypeClassMetaInfo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


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
