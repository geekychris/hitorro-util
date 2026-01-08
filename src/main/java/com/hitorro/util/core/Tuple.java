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

import com.hitorro.util.core.string.StringUtil;

import java.util.ArrayList;
import java.util.List;

/**
 */
public class Tuple<Type> {
    private Type m_vals[];
    private List<Type> m_tempList = null;

    public Type[] getVals() {
        return m_vals;
    }

    public void setVals(Type vals[]) {
        m_vals = vals;
    }

    public String[] getValsAsStringsArray() {
        return StringUtil.objectArrayToStringArray(m_vals);
    }

    public void addVal(Type o) {
        if (ListUtil.nullOrEmpty(m_tempList)) {
            m_tempList = new ArrayList<Type>();
        }
        m_tempList.add(o);
    }

    public void finalizeFromAdds() {
        if (ListUtil.nullOrEmpty(m_tempList)) {
            return;
        }
        m_vals = (Type[]) new Object[m_tempList.size()];
        m_tempList.toArray(m_vals);
        m_tempList = null;
    }
}

