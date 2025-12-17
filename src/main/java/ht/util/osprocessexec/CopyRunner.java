package ht.util.osprocessexec;

import ht.util.io.IOUtil;

import java.io.*;

/**
 * Copyright (c) 2003 - present HiTorro All rights reserved. User: chris Date: Oct 31, 2006 Time: 11:19:16 PM
 */
public class CopyRunner implements Runnable {
    private InputStream m_is = null;
    private java.io.OutputStream m_os = null;
    private String m_error = null;

    public void readFileIntoStream(java.io.OutputStream out, File inputFile)
            throws IOException {
        if (inputFile != null) {
            readStreamIntoStream(out, new FileInputStream(inputFile));
        }
    }

    public void writeToFileFromInputStream(InputStream in, File outputFile)
            throws IOException {
        if (outputFile != null) {
            writeStreamFromInputStream(in, new FileOutputStream(outputFile));
        }
    }

    public void readStreamIntoStream(java.io.OutputStream out, InputStream is) {
        m_is = is;
        m_os = out;
    }

    public void writeStreamFromInputStream(InputStream in, OutputStream os) {

        m_os = os;
        m_is = in;
    }


    public void run() {
        try {
            IOUtil.copyStream(m_is, m_os);
        } catch (IOException ioe) {
            m_error = ioe.getMessage();
        }
    }

    public String getError() {
        return m_error;
    }
}
