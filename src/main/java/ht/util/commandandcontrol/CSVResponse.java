package ht.util.commandandcontrol;

import ht.util.io.csv.CSVFileWriter;

import java.io.File;

/*
 * Copyright (c) 2003 - present HiTorro All rights reserved.
 *
 * User: chris
 */
public class CSVResponse extends Response {
    CSVFileWriter m_File_writer = null;
    private File m_file = null;

    public CSVResponse(File f) {
        m_file = f;
    }

    public void setResponseShape(ResponseShape shape) {
        super.setResponseShape(shape);
        addHeaderArray(shape.m_header.getHeader());
    }

    public void addBannerRow(String row) {

    }

    public void addHeader(String... columnHeaders) {
        addHeaderArray(columnHeaders);
    }

    @Override
    public void addStatusUpdateMessage(final String info, final int percentComplete) {
        // do nothing
    }

    public void addHeaderArray(String columnHeaders[]) {
        m_File_writer = new CSVFileWriter(m_file, columnHeaders);
    }

    public void addRow(Object... elements) {
        addRowArray(elements);
    }

    public void addRowArray(Object elements[]) {
        m_File_writer.writeRow(elements);
    }


    public void addInfo(InfoLevel level, String info) {
    }

    public void end() {
        m_File_writer.close();
    }
}
