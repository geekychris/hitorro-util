package ht.util.html.constraint;

import ht.util.core.string.StringUtil;
import ht.util.html.Link;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import java.net.URL;

/**
 * Copyright (c) 2003 - present HiTorro All rights reserved. User: chris Date: Jan 3, 2005 Time: 6:45:58 PM
 */
public class UrlStartsWithLinkConstraint implements LinkConstraint {
    private String m_start;

    public UrlStartsWithLinkConstraint(String start) {
        m_start = start;
    }

    public boolean match(String url, Link.LinkType type, String title, String typeString, Document doc, Node elem, URL source) {
        return StringUtil.startsWithIgnoreCase(url, m_start);
    }
}
