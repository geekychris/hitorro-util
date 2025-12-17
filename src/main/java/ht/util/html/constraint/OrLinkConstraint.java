package ht.util.html.constraint;

import ht.util.html.Link;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import java.net.URL;

/**
 * Copyright (c) 2003 - present HiTorro All rights reserved. User: chris Date: Jan 3, 2005 Time: 6:35:12 PM
 */
public class OrLinkConstraint extends LogicalLinkConstraint {
    public OrLinkConstraint(LinkConstraint... constraints) {
        super(constraints);
    }

    public boolean match(String url, Link.LinkType type, String title, String typeString, Document doc, Node elem, URL sourceUrl) {
        for (LinkConstraint c : m_constraints) {
            if (c.match(url, type, title, typeString, doc, elem, sourceUrl)) {
                return true;
            }
        }
        return false;
    }
}
