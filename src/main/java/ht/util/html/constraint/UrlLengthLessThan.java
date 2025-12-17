package ht.util.html.constraint;

import ht.util.html.Link;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import java.net.URL;

/**
 * <p/>
 * Copyright (c) 2003 - present HiTorro All rights reserved. User: chris Date: Sep 25, 2005 Time: 11:34:43 PM
 */
public class UrlLengthLessThan implements LinkConstraint {
    private int m_size;

    public UrlLengthLessThan(int size) {
        m_size = size;
    }

    public boolean match(String url, Link.LinkType type, String title, String typeString, Document doc, Node elem, URL sourceUrl) {
        String u = Link.expandUrl(null, url, sourceUrl);
        return u.length() <= m_size;
    }
}