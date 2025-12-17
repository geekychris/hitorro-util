package ht.util.commandandcontrol;

/**
 * Copyright (c) 2003 - present HiTorro All rights reserved. User: chris Date: Oct 31, 2006 Time: 6:49:53 PM
 */
public class TeeResponse extends Response {
    private Response m_left;
    private Response m_right;

    public TeeResponse(Response left, Response right) {
        m_left = left;
        m_right = right;
    }

    @Override
    public void addStatusUpdateMessage(final String info, final int percentComplete) {
        m_left.addStatusUpdateMessage(info, percentComplete);
        m_right.addStatusUpdateMessage(info, percentComplete);
    }

    public void setResponseShape(ResponseShape s) {
        // here we put the header.
        m_left.setResponseShape(s);
        m_right.setResponseShape(s);
    }

    public void setCommandSession(CommandSession sess) {
        m_left.setCommandSession(sess);
        m_right.setCommandSession(sess);
    }

    public void addBannerRow(String row) {
        m_left.addBannerRow(row);
        m_right.addBannerRow(row);
    }

    public void addRow(Object... elements) {
        m_left.addRow(elements);
        m_right.addRow(elements);
    }

    public void addRowArray(Object elements[]) {
        m_left.addRowArray(elements);
        m_right.addRowArray(elements);
    }

    public void addInfo(InfoLevel level, String info) {
        m_left.addInfo(level, info);
        m_right.addInfo(level, info);
    }

    public void end() {
        m_left.end();
        m_right.end();
    }
}
