package ht.util.versioning;

/**
 * Copyright (c) 2003 - present HiTorro All rights reserved. User: chris Date: Dec 5, 2006 Time: 4:51:46 PM
 */
public class VersionPartComparitor {
    private String m_s;
    private boolean m_isWildCard = false;
    private boolean m_equalToOrGreaterThan = false;
    private long m_number = 0;

    public VersionPartComparitor(String s) {
        m_s = s;
        if (m_s.equals("*")) {
            m_isWildCard = true;
            return;
        } else if (m_s.endsWith("+")) {
            m_equalToOrGreaterThan = true;
            m_s = m_s.substring(0, m_s.length() - 1);

        }
        m_number = Long.parseLong(m_s);
    }

    public VersionPartComparitor(long number) {
        m_number = number;

    }

    public boolean match(long i) {
        if (m_isWildCard) {
            return true;
        }
        if (m_equalToOrGreaterThan) {
            if (i >= m_number) {
                return true;
            }
        }
        return i == m_number;
    }
}
