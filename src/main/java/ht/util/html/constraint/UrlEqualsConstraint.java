package ht.util.html.constraint;

import ht.util.html.Link;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import java.net.URL;

/**
 * <p/>
 * Copyright (c) 2003 - present HiTorro All rights reserved. User: chris Date: Sep 25, 2005 Time: 9:16:47 PM
 */
public class UrlEqualsConstraint implements LinkConstraint {
    private String m_start;

    public UrlEqualsConstraint(String start) {
        m_start = start;
    }

    public boolean match(String url, Link.LinkType type, String title, String typeString, Document doc, Node elem, URL sourceUrl) {
        String u = Link.expandUrl(null, url, sourceUrl);
        return m_start.equalsIgnoreCase(u);
    }
}