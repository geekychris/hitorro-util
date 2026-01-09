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
package com.hitorro.util.html;


import com.hitorro.util.core.KeyValue;
import com.hitorro.util.core.ListUtil;
import com.hitorro.util.core.Log;
import com.hitorro.util.core.Timer;
import com.hitorro.util.core.string.StringUtil;
import com.hitorro.util.html.constraint.LinkConstraint;
import com.hitorro.util.html.constraint.TypeLinkConstraint;
import com.hitorro.util.html.contentsniffers.ContentSniffers;
import org.apache.tools.ant.filters.StringInputStream;
import org.apache.xerces.parsers.DOMParser;
import org.w3c.dom.Document;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

/**
 * <p/>
 * Encapsulation of a html page that has been fetched by the fetcher.
 */
public class HTMLPage {
    public static final String XPingBack = "x-pingback";
    public static final String ContentType = "content-type";
    public static final LinkConstraint PingBackConstraint = new TypeLinkConstraint(Link.LinkType.PingBack);
    private String m_url;
    private String destinationUrl;
    private String pageSource;
    private Date m_lastMod;
    private Date expirationDate;
    private int metaRefresh = 0;
    private long publishedDate;
    private long linkTime;
    private long m_parseTime;
    private Exception exceptionReason = Exception.None;
    private HTMLParser m_parser = null;
    private List<Link> m_links = null;
    private Map<String, String> m_header;
    private boolean m_truncated = false;

    public HTMLPage() {
        reset();
    }

    public void setTruncated() {
        m_truncated = true;
    }

    public boolean getTruncated() {
        return m_truncated;
    }

    public String getContentTypeFromContent() {
        return ContentSniffers.getSniffers().getMimeType(pageSource, getContentTypeFromHeader());
    }

    public void setPublishedDate(long date) {
        publishedDate = date;
    }

    public void setHeader(List<KeyValue> header) {
        if (header == null) {
            return;
        }
        Map<String, String> m = new HashMap<String, String>();
        for (KeyValue h : header) {
            if (h.getKey() == null) {
                m.put("HTTPMETHOD", h.getValue());
            } else {
                m.put(h.getKey().toLowerCase(), h.getValue());
            }

        }
        m_header = m;
    }

    public String getContentTypeFromHeader() {
        if (m_header != null) {
            return m_header.get(ContentType);
        }
        return null;
    }

    /**
     * Attempt to get the pingback url if there is one.  First we look at the http header and then in the body if we
     * enable that (that requires parsing the dom).
     *
     * @return
     */
    public String getPingBack(boolean includeHtmlBody) {
        String pb = null;
        if (m_header != null) {
            pb = m_header.get(XPingBack);
            if (!StringUtil.nullOrEmptyString(pb)) {
                return pb;
            }
            if (includeHtmlBody) {
                try {
                    List<Link> links = getLinkWithConstraint(PingBackConstraint);
                    if (!ListUtil.nullOrEmpty(links)) {
                        return links.get(0).getUrl();
                    }
                } catch (IOException e) {
                    Log.util.error("Unable to get pingback for url: %s %s %e", this.m_url, e, e);
                    return null;
                }
            }

        }
        return null;
    }


    /**
     * Time in millis to retrieve content.
     *
     * @return
     */
    public long getLinkTime() {
        return linkTime;
    }

    public void setLinkTime(long time) {
        linkTime = time;
    }

    public long getParseTime() {
        return m_parseTime;
    }

    public Exception getExceptionReason() {
        return exceptionReason;
    }

    public void setExceptionReason(Exception reason) {
        exceptionReason = reason;
    }

    public HTMLParser getParser() throws IOException {
        if (m_parser == null) {
            Timer t = new Timer();
            m_parser = new HTMLParser();
            m_parser.setHtmlPage(pageSource);
            t.stop();
            m_parseTime = t.getTime();
            m_parser.setPublishedDate(this.publishedDate);
        }

        return m_parser;
    }

    public Document parseXMLDirect() {
        DOMParser parser = new DOMParser();
        try {
            // parser.setEntityResolver(new R());
            //parser.setFeature("http://xml.org/sax/features/validation", false);
            InputStream is = new StringInputStream(pageSource);
            InputSource source = new InputSource();
            source.setByteStream(is);
            parser.parse(source);
            return parser.getDocument();
        } catch (SAXException e) {
            Log.httpfetcher.error("Unable to parse xml %s %e", e, e);
        } catch (IOException e) {
            Log.httpfetcher.error("Unable to parse xml %s %e", e, e);
        }
        return null;
    }

    /**
     * Get the xml dom of the html source.  If not already parsed it will be parsed. Parsing only happens once.
     *
     * @return
     * @throws IOException
     */
    public Document getXMLDocument() throws IOException {
        HTMLParser p = getParser();
        if (p != null) {
            return p.getDocument();
        }
        return null;
    }

    /**
     * Count of how many redirects were followed
     */
    public void incrementRefresh() {
        metaRefresh++;
    }

    public Date getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(Date expirationDate) {
        this.expirationDate = expirationDate;
    }

    public void reset() {
        m_url = null;
        pageSource = null;
        m_lastMod = null;
    }

    public String getSource() {
        return pageSource;
    }

    public void setSource(String src) {
        pageSource = src;
        m_parser = null;
    }

    /**
     * Non encapsulated source...such as from an rss feed.
     *
     * @param src
     */
    public void setSourceFromPlainText(String src) {
        setSource(StringUtil.strcat("<html><body>", src, "</body></html>"));
    }

    /**
     * Get all the links that occur in the page.  The content must be parsed into an xml dom. If you wish to get a
     * subset of links then use the z#getLinkWithConstraint.
     * <p/>
     * URL's will of been translated into their absolute form if they were relative.
     *
     * @return
     * @throws IOException
     */
    public List<Link> getLinks() throws IOException {
        return getLinkWithConstraint(null);
    }

    /**
     * Get all the links in their absolute form, only returns those links that meet the provided criteria.  This is a
     * visitor pattern using constraints that can be expressed as an expression tree. For example one can express:
     * <p/>
     * AND(OR (Type(Alternate), Type(PingBack)), startsWith(http://)) This is pseudo code, of course, you need to new up
     * constraint objects, but the idea is that you can use a number of simple constraints and group them using ands,
     * or's, not's, starts with, type etc...write your own if you get kicks!
     *
     * @return
     * @throws IOException
     */
    public List<Link> getLinkWithConstraint(LinkConstraint constraint) throws IOException {
        return getParser().getLinks(HTMLParser.PAGE_HTML, new URL(m_url), constraint, null);
    }

    /**
     * Get a listFiles of links, or null if the source url was bogus.
     *
     * @param constraint
     * @param sortDistinctComparitor
     * @return
     * @throws IOException
     */
    public List<Link> getLinkWithConstraint(LinkConstraint constraint, Comparator<Link> sortDistinctComparitor) throws IOException {
        URL url = null;
        try {
            url = new URL(m_url);
        } catch (MalformedURLException mue) {
            Log.httpfetcher.error("Unable to parse url: %s, with error: %s", m_url, mue);
            return null;
        }
        return getParser().getLinks(HTMLParser.PAGE_HTML, url, constraint, sortDistinctComparitor);
    }

    public String getUrl() {
        return m_url;
    }

    public void setUrl(String url) {
        m_url = url;
    }

    public String getDestinationUrl() {
        return destinationUrl;
    }

    public void setDestinationUrl(String destinationUrl) {
        this.destinationUrl = destinationUrl;
    }

    public Date getLastModified() {
        return m_lastMod;
    }

    public void setLastModified(Date last) {
        m_lastMod = last;
    }

    public void setMetaRefreshes(int metaRefreshes) {
        metaRefresh = metaRefreshes;
    }


    public enum Exception {
        None, IOException, HttpException, Timeout
    }
}

class R implements EntityResolver {

    public InputSource resolveEntity(String string, String string1) {
        return null;
    }
}