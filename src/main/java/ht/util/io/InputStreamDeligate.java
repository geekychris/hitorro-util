package ht.util.io;

import java.io.IOException;
import java.io.InputStream;

/**
 * Basic CInputStream deligation class that allows you to overide any of the methods you wish to intercept
 */
public abstract class InputStreamDeligate extends InputStream {
    private final InputStream m_inputStream;

    public InputStreamDeligate(InputStream inputStream) {
        m_inputStream = inputStream;
    }

    @Override
    public int read() throws IOException {
        return m_inputStream.read();
    }


    @Override
    public int read(byte[] bytes) throws IOException {
        return m_inputStream.read(bytes);
    }

    @Override
    public int read(byte[] bytes, int i, int i1) throws IOException {
        return m_inputStream.read(bytes, i, i1);
    }

    @Override
    public long skip(long l) throws IOException {
        return m_inputStream.skip(l);
    }

    @Override
    public int available() throws IOException {
        return m_inputStream.available();
    }

    @Override
    public void close() throws IOException {
        m_inputStream.close();
    }

    @Override
    public void mark(int i) {
        m_inputStream.mark(i);
    }

    @Override
    public void reset() throws IOException {
        m_inputStream.reset();
    }

    @Override
    public boolean markSupported() {
        return m_inputStream.markSupported();
    }
}
