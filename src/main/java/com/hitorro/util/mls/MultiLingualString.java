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
package com.hitorro.util.mls;

import com.hitorro.util.core.Env;
import com.hitorro.util.core.string.LangUtil;
import com.hitorro.util.core.string.StringUtil;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

/**
 * Copyright (c) HiTorro 2003-2008, Inc.
 * <p/>
 * User: chris Date: Apr 28, 2004 Time: 7:44:54 PM
 * <p/>
 * Description:
 */
public class MultiLingualString {
    private Map m_strings = new Hashtable();
    private String defaultString = null;

    public static MultiLingualString readFromStream(DataInputStream dis) throws IOException {
        int count = dis.readInt();
        MultiLingualString mls = new MultiLingualString();
        if (count == 0) {
            return mls;
        }

        for (int i = 0; i < count; i++) {
            mls.addLanguage(dis.readUTF(), dis.readUTF());
        }
        return mls;
    }

    public void addLanguage(String string, String language) {
        m_strings.put(language, string);
    }

    public String getStringNonDefaulting(String lang) {
        return (String) m_strings.get(lang);
    }

    public Set getLanguages() {
        return m_strings.keySet();
    }

    /*
        Number of translations in string
    */
    public int size() {
        return m_strings.size();
    }

    public String getDefaultString() {
        if (StringUtil.nullOrEmptyString(defaultString)) {
            return getString(Env.getSystemLanguage());
        }
        return defaultString;
    }

    public void setDefaultString(String string) {
        defaultString = string;
    }

    /*
        Given a locale like fr_ca, looks for fr_ca, fr, en_us, en
        for a string
    */
    public String getString(String lang) {
        String result = getStringOrRootLanguage(lang);
        if (result != null) {
            return result;
        }
        return getStringOrRootLanguage(Env.getSystemLanguage());
    }

    public boolean writeToStream(DataOutputStream dos) throws IOException {
        Set langs = getLanguages();
        Object[] array = langs.toArray();
        // write amount of entries
        dos.writeInt(array.length);
        for (int i = 0; i < array.length; i++) {
            String lang = (String) array[i];
            dos.writeUTF(lang);
            dos.writeUTF(getStringNonDefaulting(lang));
        }
        return true;
    }

    private String getStringOrRootLanguage(String lang) {
        String result = getStringNonDefaulting(lang);
        if (result != null) {
            return result;
        }
        String root = LangUtil.getRootOfLocaleName(lang);
        if (root != null) {
            return getStringNonDefaulting(root);
        }
        return null;
    }


}
