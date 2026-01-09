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

import com.hitorro.util.core.KeyValue;

import java.util.ArrayList;
import java.util.List;

/**
 */
public class UrlCreator {
    private StringBuilder m_buffer = new StringBuilder();
    private String m_url;
    private List<KeyValue> m_args = new ArrayList<KeyValue>();
    private String fullUrl;

    public void addArg(String argKey, String argValue) {
        m_args.add(new KeyValue(argKey, argValue));
        fullUrl = null;
    }

    public String getUrl() {
        if (fullUrl == null) {
            m_buffer.setLength(0);
            m_buffer.append(m_url);
            m_buffer.append("?");
            boolean flag = false;
            for (KeyValue kv : m_args) {
                if (flag == true) {
                    m_buffer.append("&");
                }
                m_buffer.append(kv.getKey());
                m_buffer.append("=");
                m_buffer.append(kv.getValue());
                flag = true;
            }
            fullUrl = m_buffer.toString();
        }
        return fullUrl;
    }

    public void setUrl(String url) {
        m_url = url;
        m_args.clear();

        fullUrl = null;


    }
}
