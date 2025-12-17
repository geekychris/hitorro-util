package ht.util.io.largedata.blob;

import ht.util.core.Log;

import java.io.DataInputStream;
import java.io.IOException;

/**
 * Copyright (c) 2003 - present HiTorro All rights reserved. User: chris Date: Jan 13, 2005 Time: 9:54:43 AM
 */
public class BlobLogReader {
    private DataInputStream m_dis;

    public BlobLogReader(DataInputStream dis) {
        m_dis = dis;
    }

    /**
     * read into a byte array the next blob.  If a buffer is provided AND the the blob will fit, then the provided
     * buffer will be used.  If not, a new array will be used.
     *
     * @return
     * @throws java.io.EOFException if this input stream reaches the end before reading eight bytes.
     * @throws java.io.IOException  if an I/O error occurs.
     */
    public byte[] read(byte[] buffer)
            throws IOException {
        long length = m_dis.readLong();
        if (buffer == null || buffer.length < length) {
            // not big enough, grow
            buffer = new byte[(int) length];
        }
        int lengthRead = m_dis.read(buffer, 0, (int) length);
        if (lengthRead != length) {
            Log.util.error("BlobLogReader.read could not read blob, expected length %s got %s",
                    length, lengthRead);
        }
        return buffer;
    }
}
