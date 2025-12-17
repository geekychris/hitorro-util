/*
 * Copyright (c) 2006-2025 Chris Collins
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.hitorro.util.versioning;

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
