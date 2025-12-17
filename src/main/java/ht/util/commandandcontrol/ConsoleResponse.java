package ht.util.commandandcontrol;

import ht.util.core.Constants;
import ht.util.core.string.StringUtil;

import java.io.PrintWriter;

/*
 * Copyright (c) 2003 - present HiTorro All rights reserved.
 *
 * User: chris
 */
public class ConsoleResponse extends Response {
    private static final char PadChar = ' ';
    StringBuilder m_builder = new StringBuilder();
    private boolean m_endCalled = false;
    private PrintWriter m_os;
    private int m_paddingFromHeader;
    private int m_columnSize[];
    private String m_columnSeperator = "";
    private String m_endOfLineSeperator = "";

    public ConsoleResponse() {
        m_os = new PrintWriter(System.out);
    }

    public ConsoleResponse(PrintWriter os, int paddingFromHeader) {
        m_os = os;
        m_paddingFromHeader = paddingFromHeader;
    }

    public void setCommandSession(CommandSession cs) {
        m_columnSeperator = cs.getVarAsString("columseperator");
        m_columnSeperator = StringUtil.ifNullOrEmptyReplace(m_columnSeperator, "");
        m_endOfLineSeperator = cs.getVarAsString("rowseperator");
        m_endOfLineSeperator = StringUtil.ifNullOrEmptyReplace(m_endOfLineSeperator, "");
    }

    @Override
    public void addBannerRow(String row) {
        m_os.print(row);
        m_os.print(Constants.CarriageReturnLineFeed);
    }

    public void addStatusUpdateMessage(final String info, final int percentComplete) {
        m_os.println();
        if (percentComplete != -1) {
            m_os.print(percentComplete);
        }
        m_os.print(info);
        m_os.print(Constants.CarriageReturnLineFeed);
    }

    public void addInfo(InfoLevel level, String info) {
        m_os.println();
        m_os.print(level.name());
        m_os.print(info);
        m_os.print(Constants.CarriageReturnLineFeed);
    }

    public void setResponseShape(ResponseShape shape) {
        super.setResponseShape(shape);
        addHeaderArray(shape.m_header.getHeader());
    }

    public void addHeader(String... headers) {
        addHeaderArray(headers);
    }

    public void addHeaderArray(String headers[]) {
        m_columnSize = new int[headers.length];

        for (int i = 0; i < headers.length; i++) {
            Object o = headers[i];
            int l = m_builder.length();
            StringUtil.pad(o, PadChar, m_paddingFromHeader, m_builder);
            m_columnSize[i] = m_builder.length() - l;
        }
    }

    public void addRow(Object... elements) {
        addRowArray(elements);
    }

    @Override
    public void addRowArray(Object elements[]) {
        m_builder.setLength(0);
        int min = Math.min(elements.length, m_columnSize.length);
        for (int i = 0; i < min; i++) {
            Object o = elements[i];
            if (i > 0) {
                m_builder.append(m_columnSeperator);
            }
            StringUtil.padToLength(o, PadChar, m_columnSize[i], m_builder);
        }
        m_os.print(m_builder.toString());
        m_os.print(m_endOfLineSeperator);
        m_os.print(Constants.CarriageReturnLineFeed);
    }

    @Override
    public void end() {
        if (m_endCalled) {
            return;
        }
        m_endCalled = true;
        m_os.flush();
    }
}
