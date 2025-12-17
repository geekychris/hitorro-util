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

import com.hitorro.util.commandandcontrol.serialized.InfoRow;
import com.hitorro.util.io.StoreException;
import com.hitorro.util.typesystem.HTObjectInputStream;
import com.hitorro.util.typesystem.HTObjectOutputStream;
import com.hitorro.util.typesystem.annotation.TypeClassMetaInfo;

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


