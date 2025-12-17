package ht.util.urlparser;

import ht.util.core.string.StringUtil;

/**
 * Copyright (c) 2003 - present HiTorro All rights reserved. User: chris Date: Dec 24, 2005 Time: 11:52:34 AM Encapsulation
 * of a single parameter that can be found in a uri processed by the UrlCursor.  This parameter is case free. parameters
 * are collected by the UrlCursorParameters object.
 */
public class UrlCursorParameter implements Comparable<UrlCursorParameter> {
    protected String token;
    protected long hash;
    protected String value;

    public UrlCursorParameter(String key) {
        token = key;
        hash = StringUtil.hashStringCaseFree(key);
    }

    public String getValue() {
        return value;
    }

    public void setValue(String val) {
        value = val;
    }

    public void reset() {
        value = null;
    }

    /**
     * test that the key part of the argumen is the same (if its an argument)
     *
     * @param umc
     * @return
     */
    public boolean isKey(UrlCursor umc) {
        if (!(umc.getUrlPartType() == UrlCursor.Part.Argument)) {
            return false;
        }

        return umc.isKeyPartTokenSameIgnoreCase(token);
    }

    public int compareTo(final UrlCursorParameter parameter) {
        if (hash < parameter.hash) {
            return -1;
        }
        if (hash > parameter.hash) {
            return 1;
        }
        return 0;
    }
}
