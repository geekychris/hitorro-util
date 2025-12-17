package ht.util.html.constraint;

import ht.util.html.Link;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import java.net.URL;

/**
 * Copyright (c) 2003 - present HiTorro All rights reserved. User: chris Date: Jan 3, 2005 Time: 6:38:19 PM
 */
public class NotLinkConstraint implements LinkConstraint {
    private LinkConstraint m_constraint;

    public NotLinkConstraint(LinkConstraint c) {
        m_constraint = c;
    }

    public boolean match(String url, Link.LinkType type, String title, String typeString, Document doc, Node elem, URL sourceUrl) {
        return !m_constraint.match(url, type, title, typeString, doc, elem, sourceUrl);
    }
}
