package ht.util.core.iterator;

import java.io.IOException;
import java.io.LineNumberReader;
import java.io.Reader;

public class LineReaderIterator extends AbstractIterator<String> {
    private String m_currentRow = null;

    private boolean m_open = true;

    private LineNumberReader m_reader;

    public LineReaderIterator(Reader reader) {
        m_reader = new LineNumberReader(reader);
    }

    public boolean hasNext() {
        return readAux();
    }

    public String next() {
        readAux();
        String returnThis = m_currentRow;
        m_currentRow = null;
        return returnThis;
    }

    private boolean readAux() {
        if (!m_open) {
            // closed
            return false;
        }
        if (m_currentRow == null) {
            try {
                m_currentRow = m_reader.readLine();
                if (m_currentRow == null) {
                    m_open = false;
                    m_reader.close();
                    return false;
                }
            } catch (IOException ioe) {
                m_open = false;
                return false;
            }
        }
        return true;
    }

    public void remove() {
        // Not implemented
        assert false;
    }

    @Override
    public void close() throws Exception {
        m_reader.close();
    }
}
