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
package com.hitorro.util.core;

import java.util.HashMap;

/**
 *
 */
public enum LogicalOperEnum {
    Xor("^") {
        @Override
        public boolean oper(final boolean d1, final boolean d2) {
            return d1 ^ d2;
        }

    },
    Or("||") {
        @Override
        public boolean oper(final boolean d1, final boolean d2) {
            return d1 || d2;
        }
    },
    And("&&") {
        @Override
        public boolean oper(final boolean d1, final boolean d2) {
            return d1 && d2;
        }
    };


    private static HashMap<String, LogicalOperEnum> s_byShortName;
    private String m_name;

    LogicalOperEnum(String name) {
        m_name = name.toLowerCase();
        setMapEntry(this);
    }

    public static LogicalOperEnum get(String name) {
        return s_byShortName.get(name.toLowerCase());
    }

    public static int size() {
        return s_byShortName.size();
    }

    private static void setMapEntry(LogicalOperEnum filter) {
        if (s_byShortName == null) {
            s_byShortName = new HashMap<String, LogicalOperEnum>();
        }
        s_byShortName.put(filter.getName(), filter);
    }

    public abstract boolean oper(boolean d1, boolean d2);

    public String getName() {
        return m_name;
    }
}
