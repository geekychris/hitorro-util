package ht.util.html.constraint;

import ht.util.core.string.StringUtil;
import ht.util.html.Link;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import java.net.URL;

/**
 * Copyright (c) 2003 - present HiTorro All rights reserved. User: chris Date: Jan 4, 2005 Time: 9:24:41 AM
 * <p/>
 * Determines if this link is a relative or absolute value.  For it to be an absolute link, it has to start with http.
 */
public class RelativeLinkConstraint implements LinkConstraint {
    public boolean match(String url, Link.LinkType type, String title, String typeString, Document doc, Node elem, URL source) {
        return !StringUtil.startsWithIgnoreCase(url, "http");
    }
}
