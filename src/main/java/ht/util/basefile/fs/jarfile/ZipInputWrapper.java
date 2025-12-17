package ht.util.basefile.fs.jarfile;

import java.io.IOException;
import java.io.InputStream;

/**
 *
 */
public class ZipInputWrapper extends InputStream {
    private InputStream m_is;

    public ZipInputWrapper(InputStream is) {
        m_is = is;
    }

    public int read() throws IOException {
        return m_is.read();
    }

    public int read(byte b[]) throws IOException {
        return m_is.read(b, 0, b.length);
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
        return m_is.read(b, off, len);
    }

    public long skip(long n) throws IOException {
        return m_is.skip(n);
    }


    public int available() throws IOException {
        return m_is.available();
    }

    public void close() throws IOException {
        // do nothing
        // we dont want the underlying object to have close called on it
    }

    public synchronized void mark(int readlimit) {
        m_is.mark(readlimit);
    }

    public synchronized void reset() throws IOException {
        m_is.reset();
    }

    public boolean markSupported() {
        return m_is.markSupported();
    }
}
