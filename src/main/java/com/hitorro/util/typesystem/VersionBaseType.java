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

import jakarta.persistence.*;

import java.io.IOException;

/**
 */
@MappedSuperclass
public abstract class VersionBaseType<T extends BaseSession> extends BaseType<T> {
    private transient boolean markedFlush = false;

    private transient int versionStampSnapshot;

    @Version
    @Column(name = "versionStamp")
    private Integer versionStamp;

    public int getVersionStamp() {
        return versionStamp != null ? versionStamp : 0;
    }

    public void setVersionStamp(int version) {
        versionStamp = version;
    }


    public void snapshotVersionStamp() {
        versionStampSnapshot = versionStamp != null ? versionStamp : 0;
    }

    public void recoverSnapshotVersionStamp() {
        versionStamp = versionStampSnapshot;
    }

    public void markFlushed() {
        markedFlush = true;
    }

    public boolean isMarkedFlushed() {
        return markedFlush;
    }

    public void resetMarkedFlushed() {
        markedFlush = false;
    }

    public void serialize(HTObjectOutputStream os)
            throws IOException, StoreException {
        super.serialize(os);
        os.writeInt(versionStamp != null ? versionStamp : 0);
    }

    public void deserialize(HTObjectInputStream os)
            throws IOException, ClassNotFoundException, StoreException {
        int version = os.readInt();
        super.deserialize(os);
        switch (version) {
            case 1:
                versionStamp = os.readInt();
        }
    }

    public String getGuid() {
        return null;
    }

    public void setGuid(String guid) {

    }
}
