package ht.util.core;

import ht.util.core.string.StringUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Copyright (c) 2003 - present HiTorro All rights reserved. User: chris Date: Oct 31, 2006 Time: 11:34:29 PM
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

