package ht.util.io;

import java.io.IOException;
import java.io.InputStream;

/**
 * Copyright (c) 2003 - present HiTorro All rights reserved. User: chris Date: Nov 1, 2006 Time: 4:00:56 PM
 * <p/>
 * Wrapper around an input stream to limit the amount of bytes that can be read.  This is usefull for streams within
 * streams.
 */
public class LengthConstrainedInputStream extends InputStream {
    private long m_bytesToRead;
    private InputStream m_is;

    public LengthConstrainedInputStream(InputStream is, long bytesToRead) {
        m_is = is;
        m_bytesToRead = bytesToRead;
    }

    public int read() throws IOException {
        return m_is.read();
    }


    public int read(byte b[]) throws IOException {
        return read(b, 0, b.length);
    }

    /**
     * read limited to the amount of bytes we requested.
     *
     * @param b
     * @param off
     * @param len
     * @return
     * @throws IOException
     */
    public int read(byte b[], int off, int len) throws IOException {
        int read = 0;
        if (m_bytesToRead < len) {
            len = (int) m_bytesToRead;
        }
        if (m_bytesToRead == 0) {
            return -1;
        }
        read = m_is.read(b, off, len);
        if (read == -1) {
            return -1;
        }
        m_bytesToRead -= read;
        return read;
    }

    public long skip(long n) throws IOException {
        if (m_bytesToRead < n) {
            n = (int) m_bytesToRead;
        }
        m_bytesToRead = 0;
        return m_is.skip(n);
    }


    public int available() {
        return 0;
    }

    public void close() throws IOException {
        // do nothing
    }

    public synchronized void mark(int readlimit) {
        m_is.mark(readlimit);
    }

    public synchronized void reset() throws IOException {
        throw new IOException("mark/reset not supported");
    }


    public boolean markSupported() {
        return m_is.markSupported();
    }

}
