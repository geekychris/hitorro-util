package ht.util.urlparser;

import ht.util.core.KeyValue;

import java.util.ArrayList;
import java.util.List;

/**
 * Copyright (c) 2003 - present HiTorro All rights reserved. User: chris Date: Jul 5, 2005 Time: 5:59:20 PM
 */
public class UrlCreator {
    private StringBuilder m_buffer = new StringBuilder();
    private String m_url;
    private List<KeyValue> m_args = new ArrayList<KeyValue>();
    private String m_fullUrl;

    public void addArg(String argKey, String argValue) {
        m_args.add(new KeyValue(argKey, argValue));
        m_fullUrl = null;
    }

    public String getUrl() {
        if (m_fullUrl == null) {
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
            m_fullUrl = m_buffer.toString();
        }
        return m_fullUrl;
    }

    public void setUrl(String url) {
        m_url = url;
        m_args.clear();

        m_fullUrl = null;


    }
}
