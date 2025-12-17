package ht.util.core.iterator;

import java.io.IOException;
import java.io.StreamTokenizer;

public class StreamTokenizerIterator extends AbstractIterator<String> {
    private StreamTokenizer m_tokenizer;

    private String m_token;

    private boolean m_hasToken;

    public StreamTokenizerIterator(StreamTokenizer tokenizer) {
        m_tokenizer = tokenizer;
        getToken();
    }

    public boolean hasNext() {
        return m_hasToken;
    }

    public String next() {
        String o = m_token;
        getToken();
        return o;
    }

    public void remove() {

    }

    private boolean getToken() {
        try {
            if (m_tokenizer.nextToken() != StreamTokenizer.TT_EOF) {
                m_hasToken = true;
                switch (m_tokenizer.ttype) {

                    case StreamTokenizer.TT_NUMBER:
                        m_token = Double.toString(m_tokenizer.nval);
                        break;
                    case StreamTokenizer.TT_WORD:
                        m_token = m_tokenizer.sval; // Already a String
                        break;
                    default: // single character in ttype
                        m_token = String.valueOf((char) m_tokenizer.ttype);
                }
            } else {
                m_hasToken = false;
            }
        } catch (IOException e) {
            m_hasToken = false;
        }
        return m_hasToken;
    }

    @Override
    public void close() throws Exception {
    }
}
