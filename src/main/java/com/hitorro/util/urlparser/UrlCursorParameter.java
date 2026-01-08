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
package com.hitorro.util.urlparser;

import com.hitorro.util.core.string.StringUtil;

/**
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
