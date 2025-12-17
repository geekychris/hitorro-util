package ht.util.io;

import java.io.IOException;
import java.io.OutputStream;

/**
 * allow a stream to goto two places.
 *
 * @author chris
 */
public class TeeOutputStream extends OutputStream {
    private OutputStream m_a;
    private OutputStream m_b;

    public TeeOutputStream(OutputStream a, OutputStream b) {
        m_a = a;
        m_b = b;
    }

    @Override
    public void write(int arg0) throws IOException {
        m_a.write(arg0);
        m_b.write(arg0);
    }

    public void close() throws IOException {
        m_a.close();
        m_b.close();
    }

    public void flush() throws IOException {
        m_a.flush();
        m_b.flush();

    }

    public void write(byte[] b) throws IOException {
        m_a.write(b);
        m_b.write(b);
    }

    public void write(byte[] b, int off, int len) throws IOException {
        m_a.write(b, off, len);
        m_b.write(b, off, len);
    }


}