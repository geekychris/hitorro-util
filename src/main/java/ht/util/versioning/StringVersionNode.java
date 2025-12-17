package ht.util.versioning;

/**
 * Copyright (c) 2003 - present HiTorro All rights reserved. User: chris Date: Dec 5, 2006 Time: 5:08:53 PM
 * <p/>
 * Test node for unit testing versioning.
 */
public class StringVersionNode extends VersionNode {
    private String m_s;

    public StringVersionNode(String s, int major, int minor, int patch, int buildNumber) {
        super(major, minor, patch, buildNumber);
        m_s = s;

    }

    public String getString() {
        return m_s;
    }
}
