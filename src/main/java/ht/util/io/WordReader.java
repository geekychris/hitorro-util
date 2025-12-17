package ht.util.io;

import ht.util.io.largedata.compressedstreams.CInputStream;

import java.io.IOException;

/**
 * Copyright (c) HiTorro 2003-2008, Inc.
 * <p/>
 * User: chris Date: May 11, 2004 Time: 10:52:22 AM
 * <p/>
 * Description:
 */
public class WordReader {

    private int BUFFER_SIZE = 1024;
    private int m_bytesRead;
    private boolean m_moreBytesToRead = true;
    private CInputStream m_stream;
    private char[] m_buff = new char[BUFFER_SIZE];

    public WordReader(CInputStream is) {
        m_stream = is;
    }

    private boolean fillBuffer()
            throws IOException {
        if (m_moreBytesToRead == false) {
            return false;
        }
        //m_bytesRead = m_stream.readChars(m_buff, 0, BUFFER_SIZE);
        if (m_bytesRead > 0) {
            return true;
        }
        return false;
    }
}
