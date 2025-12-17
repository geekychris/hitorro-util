package ht.util.io.largedata.blob;

import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Copyright (c) 2003 - present HiTorro All rights reserved. User: chris Date: Jan 13, 2005 Time: 9:56:41 AM
 */
public class BlobLogWriter {
    private DataOutputStream m_dos;

    public BlobLogWriter(DataOutputStream dos) {
        m_dos = dos;
    }

    public void write(byte[] buff) throws IOException {
        write(buff, 0, buff.length);
    }

    public void write(byte[] buff, int startPos, int length) throws IOException {
        m_dos.writeLong(length);
        m_dos.write(buff, startPos, length);
    }
}
