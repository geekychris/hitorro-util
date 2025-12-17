package ht.util.html.constraint;

import gnu.trove.map.hash.TLongIntHashMap;
import ht.util.core.hash.FPHash64;
import ht.util.core.string.StringUtil;
import ht.util.html.Link;
import ht.util.urlparser.URLUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import java.net.URL;

/**
 * Copyright (c) 2003 - present HiTorro All rights reserved. User: chris Date: Mar 2, 2005 Time: 7:51:21 AM
 * <p/>
 * Test to see if a url comes from a certain site.
 */
public class UrlFromSite implements LinkConstraint {
    private TLongIntHashMap m_map;

    public UrlFromSite(TLongIntHashMap siteMap) {
        m_map = siteMap;
    }

    public boolean match(String url, Link.LinkType type, String title, String typeString, Document doc, Node elem, URL source) {
        String site = URLUtil.getSiteFromURL(url);
        if (StringUtil.nullOrEmptyString(site)) {
            return false;
        }
        long fp = FPHash64.getFP(site.toLowerCase());
        return m_map.contains(fp);
    }


}
