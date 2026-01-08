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
package com.hitorro.util.html.constraint;

import gnu.trove.map.hash.TLongIntHashMap;
import com.hitorro.util.core.hash.FPHash64;
import com.hitorro.util.core.string.StringUtil;
import com.hitorro.util.html.Link;
import com.hitorro.util.urlparser.URLUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import java.net.URL;

/**
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
