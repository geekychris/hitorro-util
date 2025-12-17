package ht.util.html.constraint;

import ht.util.html.Link;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import java.net.URL;

/**
 * Copyright (c) 2003 - present HiTorro All rights reserved. User: chris Date: Jan 3, 2005 Time: 6:40:39 PM
 */
public class TypeLinkConstraint implements LinkConstraint {
    private Link.LinkType m_type;

    public TypeLinkConstraint(Link.LinkType type) {
        m_type = type;
    }

    public boolean match(String url, Link.LinkType type, String title, String typeString, Document doc, Node elem, URL source) {
        return type == m_type;
    }
}

