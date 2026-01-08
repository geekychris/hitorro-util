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

import com.hitorro.util.basefile.fs.BaseFile;
import com.hitorro.util.core.GenericKeyValue;
import com.hitorro.util.core.ListUtil;
import com.hitorro.util.core.Log;
import com.hitorro.util.core.map.MapUtil;
import com.hitorro.util.core.string.StringBuilderUtil;
import com.hitorro.util.core.string.StringUtil;
import com.hitorro.util.html.constraint.LinkConstraint;
import com.hitorro.util.html.constraint.TypeLinkConstraint;
import org.apache.xerces.xni.parser.XMLInputSource;
import org.cyberneko.html.parsers.DOMParser;
import org.w3c.dom.*;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

/**
 * HTML Parser using the CyberNecko Parser.
 * <p/>
 */

public class HTMLParser {
    public static final String PAGE_TITLE = "TITLE";
    public static final String PAGE_META = "META";
    public static final String PAGE_META_KEYWORDS = "META-KEYWORDS";
    public static final String PAGE_META_DESCRIPTION = "META-DESCRIPTION";
    public static final String PAGE_BODY = "BODY";
    public static final String PAGE_HTML = "HTML";
    public static final String ENCODING = "UTF-8";
    public static final int BUF_SIZE = 1024;
    public static final char SEPARATOR = '\n';
    /**
     * Setup the filters to use for this HTMLParser instance.
     */
    private static String[] Filters = {"SCRIPT", "STYLE"};
    private String page;
    private DOMParser parser;
    private Map<String, String> filters = null;

    private long publishDate;
    private char seperator = SEPARATOR;

    /**
     * Default constructor
     */
    public HTMLParser() {
        initFilters(Filters);
    }

    public HTMLParser(char seperator) {
        initFilters(Filters);
        this.seperator = seperator;
    }

    /**
     * Creates an object to parse a String of (possibly) broken HTML code. There can be only one <html> tag within
     * <code>page</code>.
     *
     * @param htmlString HTML page to be parsed
     * @throws java.io.IOException
     */
    public HTMLParser(String htmlString) throws IOException {
        initFilters(Filters);
        setHtmlPage(htmlString);
    }

    public HTMLParser(BaseFile file) throws IOException {
        if (!file.exists()) {
            return;
        }

        initFilters(Filters);


        String buff = file.readString();
        setHtmlPage(buff);
    }

    public HTMLParser(File file) throws IOException {
        if (!file.exists()) {
            return;
        }

        initFilters(Filters);

        StringBuilder sb = new StringBuilder();
        StringBuilderUtil.readFileIntoBuilder(sb, file);
        setHtmlPage(sb.toString());
    }

    /**
     * Rough scan to see if we think it contains tags
     *
     * @param data
     * @return
     */
    public static final boolean testForXML(String data) {
        int indexOf = data.indexOf("<");
        while (indexOf != -1) {
            if (StringUtil.sloppyIndexOf(data, indexOf + 1, 20, '>') != -1) {
                return true;
            }
            indexOf = data.indexOf("<", indexOf + 1);
        }
        return false;
    }

    public void setPublishedDate(long date) {
        publishDate = date;
    }

    public void setFilters(String filters[]) {
        initFilters(filters);
    }

    /**
     * Reset the parser to its initial state. Release all resources from a previous parse so we can use it again
     */
    public void reset() {
        this.page = null;
        this.parser.reset();
    }

    /**
     * Determine if this page is a redirect
     *
     * @param url
     * @return
     * @throws java.net.MalformedURLException
     */
    public String getRedirectURL(String url) throws MalformedURLException {

        Document doc = getDocument();
        NodeList nlist = doc.getElementsByTagName("meta");
        for (int i = 0; i < nlist.getLength(); ++i) {
            Element e = ((Element) nlist.item(i));
            String hequiv = e.getAttribute("http-equiv");
            if (StringUtil.notNullEquals(hequiv, "refresh", true)) {
                String content = e.getAttribute("content");
                if (content != null) {

                    // make sure the tag is not enclosed in <NOSCRIPT>
                    Node curr = e;
                    if (containedWithin(e, curr, "script")) {
                        continue;
                    }
                    String urlRedirect = e.getAttribute("url");
                    String parts[] = content.split(";");
                    if ((parts.length == 2) && parts[1].length() > 4 && parts[1].trim().substring(0, 4).equalsIgnoreCase("url=")) {
                        String part1 = parts[1].trim();
                        String newURL = part1.substring("url=".length(), part1.length());

                        // handle relative URLs
                        java.net.URL fromURL = new URL(url);
                        java.net.URL toURL = new URL(fromURL, newURL);
                        return toURL.toString();
                    }
                }
            }
        }
        return null;
    }

    private boolean containedWithin(Element e, Node curr, String nodeName) {
        while (true) {
            Node parent = e.getParentNode();
            if (parent != curr) {
                if (parent instanceof Element) {
                    String tag = ((Element) parent).getTagName();
                    if (tag.toLowerCase().indexOf(nodeName) != -1) {
                        // possibly conditional, ignore
                        return true;
                    }
                }
                curr = parent;
            } else {
                return false;
            }
        }
    }

    /**
     * Returns a reference to the raw HTML page being processed.
     *
     * @return String containing the raw HTML page being processed
     */
    public String getHtmlPage() {
        return this.page;
    }

    /**
     * Prepares instance to process a new page of HTML text. <code>page</code> can contain only a single
     * <html>tag.
     *
     * @param htmlString String containing all HTML page including tags.
     * @throws IOException
     */
    public void setHtmlPage(String htmlString) throws IOException {
        // remove stuff before and after the html since it breaks the parser
        this.page = HTMLUtil.getHTMLStripped(htmlString);
        initialize();
    }

    public void setSourceFromPlainText(String src) throws IOException {
        this.page = StringUtil.strcat("<html><body>", src, "</body></html>");
        initialize();
    }

    /**
     * Return the DOM document object.
     *
     * @return Document (Xerces dom)
     * @throws IOException
     */
    public Document getDocument() {
        return this.parser.getDocument();
    }

    public DOMParser getDomParser() {
        return parser;
    }

    /**
     * Extracts all non-tag text from from the <BODY> element of an HTML page.
     *
     * @return String containing all non-tag text from an HTML page.
     * @throws org.w3c.dom.DOMException
     */
    public String getBodyText() throws DOMException {
        return getText(PAGE_BODY);
    }

    /**
     * Extracts all non-tag text from an entire HTML page.
     *
     * @return String containing a '\n' delimited listFiles of text blocks
     * @throws IOException
     * @see #getText(String)
     */
    public String getHtmlText() throws DOMException {
        return getText(PAGE_HTML);
    }

    /**
     * Extracts Meta Keywords from an entire HTML page.
     *
     * @return String containing a '\n' delimited listFiles of text blocks
     * @throws IOException
     * @see #getText(String)
     */
    public String getMetaKeywords() throws DOMException {
        return getText(PAGE_META_KEYWORDS);
    }

    /**
     * Extracts Meta Description text from an entire HTML page.
     *
     * @return String containing a '\n' delimited listFiles of text blocks
     * @throws IOException
     * @see #getText(String)
     */
    public String getMetaDescription() throws DOMException {
        return getText(PAGE_META_DESCRIPTION);
    }

    /**
     * Extracts all non-tag text from the <TITLE> element of an HTML page.
     *
     * @return String containing <TITLE> text
     */
    public String getTitleText() throws DOMException {
        return getText(PAGE_TITLE);
    }

    /**
     * Extracts non-tag text from all those portions of an HTML page bound to a given tag. To extract all text from the
     * page, specify a <code>tagName</code> of HTML or use the overloaded <code>getText(String)</code>.
     * <code>tagName</code> is case insensitive. Returns an empty string if the tag does not exist in the HTML page or
     * if there is not text within that element.
     * <p/>
     * Any <code>tagName</code> beginning with META- is handled differently. Any suffix to META-, for instance, KEYWORDS
     * in the <code>tagName</code> META-KEYWORDS, is interpreted as the value of the NAME attribute in a META tag, and
     * <code>getText(String)</code> returns the value of the corresponding CONTENT attribute.
     *
     * @param tagName An HTML tag identifying subset of page to be processed
     * @return String containing a '\n' delimited listFiles of text blocks
     * @throws IOException
     */
    public String getText(String tagName) throws DOMException {
        String upperCaseTagName = tagName.toUpperCase();
        StringBuilder buf = new StringBuilder(BUF_SIZE);
        Document doc = null;
        if (this.parser != null) {
            doc = this.parser.getDocument();

        }
        if (doc != null) {
            Element htmlElement = doc.getDocumentElement();
            if (upperCaseTagName.length() > 5 && upperCaseTagName.startsWith("META-")) {
                // process meta tags specially
                getMetaTag(upperCaseTagName, htmlElement, buf);
            } else if (upperCaseTagName.equals("HTML")) {
                // Recursively accumulate all text from the root
                accumulate(htmlElement, buf);
            } else {
                // Grab non-tag text from all copies of this tag
                NodeList nodeList = htmlElement.getElementsByTagName(upperCaseTagName);
                int len = nodeList.getLength();
                for (int i = 0; i < len; i++) {
                    Element element = (Element) nodeList.item(i);
                    accumulate(element, buf);
                }
            }
        }
        return buf.toString();
    }

    /**
     * Get any META tag
     *
     * @param tagName META tag name
     * @throws IOException
     */
    public String getMetaTag(String tagName) throws DOMException {
        String upperCaseTagName = tagName.toUpperCase();
        StringBuilder buf = new StringBuilder(BUF_SIZE);
        Document doc = null;
        if (this.parser != null) {
            doc = this.parser.getDocument();
        }
        if (doc != null) {
            getMetaTagName(upperCaseTagName, doc.getDocumentElement(), buf);
        }
        return buf.toString();
    }

    /**
     * Look for a Meta tag that contains <META *="..." CONTENT="..." /> where * is what we pass as META-*
     *
     * @param upperCaseTagName
     * @param htmlElement
     * @param buf
     */

    private void getMetaTag(String upperCaseTagName, Element htmlElement, StringBuilder buf) {
        getMetaTagName(upperCaseTagName.substring(5), htmlElement, buf);
    }

    /**
     * return a
     *
     * @param name
     * @param htmlElement
     * @param buf
     */
    private void getMetaTagName(String name, Element htmlElement, StringBuilder buf) {
        NodeList nodeList = htmlElement.getElementsByTagName("META");
        int len = nodeList.getLength();
        for (int i = 0; i < len; i++) {
            Element element = (Element) nodeList.item(i);
            if (element.getAttribute("NAME").equalsIgnoreCase(name)) {
                String str = element.getAttribute("CONTENT");
                appendNonXMLContent(buf, str, element.getNodeName().equalsIgnoreCase("textarea"));
                break;
            }
        }
    }

    public List<GenericKeyValue<String, List<String>>> getMetaTagNByPropertyGrouped(String name) {
        List<GenericKeyValue<String, String>> list = getMetaTagNByProperty(name);
        if (list == null) {
            return null;
        }
        List<GenericKeyValue<String, List<String>>> res = new ArrayList();
        Map<String, GenericKeyValue<String, List<String>>> map = new HashMap();
        for (GenericKeyValue<String, String> k : list) {
            GenericKeyValue<String, List<String>> kM = map.get(k.getKey());
            if (kM == null) {
                kM = new GenericKeyValue<String, List<String>>(k.getKey(), new LinkedList());
                map.put(k.getKey(), kM);
                res.add(kM);
            }
            kM.getValue().add(k.getValue());
        }
        return res;
    }

    /**
     * return a
     *
     * @param name
     */
    public List<GenericKeyValue<String, String>> getMetaTagNByProperty(String name) {

        List<GenericKeyValue<String, String>> ret = null;
        Document doc = null;
        if (this.parser != null) {
            doc = this.parser.getDocument();
        }
        Element htmlElement = doc.getDocumentElement();
        NodeList nodeList = htmlElement.getElementsByTagName("meta");
        int len = nodeList.getLength();
        for (int i = 0; i < len; i++) {
            Element element = (Element) nodeList.item(i);
            String at = element.getAttribute("property");
            if (at.startsWith(name)) {
                if (ret == null) {
                    ret = new ArrayList();
                }
                String pName = at.substring(name.length() + 1);
                String str = element.getAttribute("CONTENT");
                ret.add(new GenericKeyValue<String, String>(pName, str));
            }
        }
        return ret;
    }

    /**
     * Old method that doesnt return link objects and only returns links that are of the type anchor.
     *
     * @param tag
     * @return
     */
    public List<String> getLinks(String tag) {
        LinkConstraint lc = new TypeLinkConstraint(Link.LinkType.Anchor);
        List<Link> links = getLinks(tag, null, lc, null);
        List<String> returnMe = new ArrayList<String>();
        for (Link l : links) {
            returnMe.add(l.getUrl());
        }
        return returnMe;
    }

    public List<String> getLinks(String tag, LinkConstraint lc) {
        List<Link> links = getLinks(tag, null, lc, null);
        List<String> returnMe = new ArrayList<String>();
        for (Link l : links) {
            returnMe.add(l.getUrl());
        }
        return returnMe;
    }

    public List<Link> getLinks(String tagName, URL sourceUrl) {
        return getLinks(tagName, sourceUrl, null, null);
    }

    public List<Link> getLinks(String tagName, URL sourceUrl, LinkConstraint constraint, Comparator<Link> sortDistinctComparitor) {
        List<Link> list = new ArrayList<Link>();

        try {
            String upperCaseTagName = tagName.toUpperCase();
            Document doc = null;
            if (this.parser != null) {
                doc = this.parser.getDocument();
            }
            if (doc != null) {

                Element htmlElement = doc.getDocumentElement();
                if (upperCaseTagName.equals("HTML")) {
                    // Recursively accumulate all text from the root
                    accumulateLinks(doc, htmlElement, list, sourceUrl, constraint, 0);
                } else {
                    // Grab non-tag text from all copies of this tag
                    NodeList nodeList = htmlElement.getElementsByTagName(upperCaseTagName);
                    int len = nodeList.getLength();
                    for (int i = 0; i < len; i++) {
                        Element element = (Element) nodeList.item(i);
                        accumulateLinks(doc, element, list, sourceUrl, constraint, 0);
                    }
                }
                if (sortDistinctComparitor != null) {
                    ListUtil.removeDuplicates(list, sortDistinctComparitor);
                }

                if (sourceUrl != null) {
                    String source = sourceUrl.toString();
                    for (Link l : list) {
                        l.setPublishedDate(this.publishDate);
                        l.setSource(source);
                    }
                }
            }
        } catch (DOMException dome) {
            Log.util.error("Unable to parse page with error %s", dome.getLocalizedMessage());
        }
        return list;
    }


    private void accumulateLinks(Document doc, Element element, List<Link> list, URL sourceUrl, LinkConstraint constraint, int depth) {
        Node child = element.getFirstChild();

        while (null != child) {
            String name = child.getNodeName();
            if (name.equalsIgnoreCase("a")) {
                processAncor(child, constraint, doc, sourceUrl, list);

            } else if (name.equalsIgnoreCase("link")) {
                processLink(child, constraint, doc, sourceUrl, list);
            } else if (name.equalsIgnoreCase("img")) {
                processImage(child, constraint, doc, sourceUrl, list);
            }
            int nodeType = child.getNodeType();
            if (Node.ELEMENT_NODE == nodeType) {
                if (element == child || depth > 100) {
                    //XXX TODO Anti short circuit mechanism...cant see much having a depth of 100 or more!!!
                    return;
                }
                accumulateLinks(doc, (Element) child, list, sourceUrl, constraint, depth++);
            }

            child = child.getNextSibling();
        }

    }


    private void processImage(Node child, LinkConstraint constraint, Document doc, URL sourceUrl, List<Link> list) {// we have an a
        NamedNodeMap map = child.getAttributes();
        if (map != null) {
            Node n = map.getNamedItem("src");
            if (n != null) {
                String text = n.getTextContent();
                Link.LinkType type = Link.LinkType.Image;

                if (!StringUtil.nullOrEmptyString(text)) {
                    if (constraint == null || constraint.match(text, type, "", "image", doc, child, sourceUrl)) {
                        list.add(new Link(text, type, "", "image", sourceUrl, getAttributes(child)));
                    }

                }
            }
        }
    }

    private void processLink(Node child, LinkConstraint constraint, Document doc, URL sourceUrl, List<Link> list) {// we have an a
        NamedNodeMap map = child.getAttributes();
        if (map != null) {
            Node n = map.getNamedItem("href");
            if (n != null) {
                String text = n.getTextContent();
                Link.LinkType type = Link.LinkType.Unknown;

                Node t = map.getNamedItem("rel");
                if (t != null) {
                    String typeString = t.getTextContent().toLowerCase();
                    if (typeString.equals("alternate")) {
                        type = Link.LinkType.Alternate;
                    } else if (typeString.equals("stlesheet")) {
                        type = Link.LinkType.StyleSheet;
                    } else if (typeString.equals("pingback")) {
                        type = Link.LinkType.PingBack;
                    } else if (typeString.equals("shortcut icon")) {
                        type = Link.LinkType.ShortCutIcon;
                    } else if (typeString.equals("EditURI")) {
                        type = Link.LinkType.EditURI;
                    }
                }
                Node titleNode = map.getNamedItem("title");
                String title = "";
                if (titleNode != null) {
                    title = titleNode.getTextContent();
                }

                Node typeNode = map.getNamedItem("TYPE");
                String typeString = "";
                if (typeNode != null) {
                    typeString = typeNode.getTextContent();
                }
                if (!StringUtil.nullOrEmptyString(text)) {
                    if (constraint == null || constraint.match(text, type, title, typeString, doc, child, sourceUrl)) {
                        List<GenericKeyValue<String, String>> attr = getAttributes(child);
                        Link l = new Link(text, type, title, typeString, sourceUrl, attr);

                        list.add(l);
                    }

                }
            }
        }
    }

    private void processAncor(Node child, LinkConstraint constraint, Document doc, URL sourceUrl, List<Link> list) {// we have an a
        NamedNodeMap map = child.getAttributes();
        if (map != null) {
            Node n = map.getNamedItem("href");
            if (n != null) {
                String text = n.getTextContent();
                if (!StringUtil.nullOrEmptyString(text)) {
                    String title = "";
                    Node titleNode = map.getNamedItem("content");
                    if (titleNode != null) {
                        title = titleNode.getTextContent();
                    }
                    if (StringUtil.nullOrEmptyOrBlankString(title)) {
                        Node fChild = child.getFirstChild();
                        if (fChild != null) {
                            title = fChild.getTextContent();
                        }
                    }
                    if (constraint == null || constraint.match(text, Link.LinkType.Anchor, title, "", doc, child, sourceUrl)) {
                        List<GenericKeyValue<String, String>> attr = getAttributes(child);
                        list.add(new Link(text, Link.LinkType.Anchor, title, "", sourceUrl, attr));
                    }
                }
            }
        }
    }

    private List<GenericKeyValue<String, String>> getAttributes(Node child) {
        List<GenericKeyValue<String, String>> list = new ArrayList();
        NamedNodeMap nnm = child.getAttributes();
        int size = nnm.getLength();
        for (int i = 0; i < size; i++) {
            Node n = nnm.item(i);
            String name = n.getNodeName();
            String value = n.getNodeValue();
            list.add(new GenericKeyValue(name, value));
        }
        return list;
    }

    /**
     * Determine if we are processing a TEXTAREA and if so, check it for XML content. If we have xml content we throw it
     * away. This is based upon an observation that some websites embed xml into these areas for javascript processing.
     *
     * @param buf
     * @param str
     * @param isTextArea
     */
    private void appendNonXMLContent(StringBuilder buf, String str, boolean isTextArea) {
        if (isTextArea && testForXML(str)) {
            /* dont bother adding this, its a textarea and it contains something that looks
               like xml. We could enhance the parser to consider parsing this xml to */
            return;
        }
        buf.append(str);
    }

    /**
     * Appends non-tag text associated with <code>element</code> to an accumulation buffer. If <code>element</code> has
     * child elements, this function appends text from the subordinate elements as well.
     *
     * @param element HTML element to be processed
     * @param buf     Accumulation buffer for extracted text
     */
    private void accumulate(Element element, StringBuilder buf) {
        Node child = element.getFirstChild();

        if (!isFiltered(element.getNodeName())) {
            // only do elements that are not filtetered out by name
            while (null != child) {
                int nodeType = child.getNodeType();
                if (Node.ELEMENT_NODE == nodeType) {
                    accumulate((Element) child, buf);
                } else if (Node.TEXT_NODE == nodeType) {
                    String text = child.getNodeValue().trim();
                    if (text.length() > 0) {
                        appendNonXMLContent(buf, text, element.getNodeName().equalsIgnoreCase("textarea"));
                        buf.append(seperator);
                    }
                } else {
                    // do nothing
                }
                child = child.getNextSibling();
            }
        }
    }

    private void initFilters(String filters[]) {
        this.filters = MapUtil.createStringIdentityMap(filters);
    }

    /**
     * determine if the current tag is a filtered tag or not
     *
     * @param tag
     * @return true if the filtered tag is on the no print listFiles
     */
    private boolean isFiltered(String tag) {
        if (StringUtil.nullOrEmptyString(tag)) {
            return true;
        }
        return filters.containsKey(tag.toUpperCase());
    }

    /**
     * Creates the object that creates the DOM.
     *
     * @throws IOException
     */
    private void initialize() throws IOException {
        StringReader reader = new StringReader(this.page);
        XMLInputSource source = new XMLInputSource("", "", "", reader, ENCODING);

        this.parser = new DOMParser();
        try {
            String c1 = org.apache.html.dom.HTMLDocumentImpl.class.getCanonicalName();
            //String c2 = com.sun.org.apache.xerces.internal.dom.DeferredDocumentImpl.class.getCanonicalName();
            parser.setProperty("http://apache.org/xml/properties/dom/document-class-name", c1);
        } catch (SAXNotRecognizedException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (SAXNotSupportedException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        try {
            this.parser.setFeature("http://xml.org/sax/features/namespaces", false);
        } catch (SAXNotRecognizedException e) {
            Log.util.error("Exception %s %e", e, e);
        } catch (SAXNotSupportedException e) {
            Log.util.error("Exception %s %e", e, e);
        }

        this.parser.parse(source);
    }
}
