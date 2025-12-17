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
package com.hitorro.util.xml;

import com.hitorro.util.core.ListUtil;
import org.apache.xerces.parsers.DOMParser;
import org.w3c.dom.*;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.util.List;


/**
 * set of utility functions for parsing xml content into Documents.
 */
public class XmlDomUtil {

    /**
     * Take an input stream and give back a parsed Document or null if couldn't parse doc.
     *
     * @param inputStream
     * @return
     */
    public static final Document parseFromInputStream(InputStream inputStream) {
        return parseFromInputSource(new InputSource(inputStream));
    }

    /**
     * @param buffer
     * @return parsed document.
     */
    public static final Document parseFromString(String buffer) {

        Reader reader = new StringReader(buffer);
        InputSource is = new InputSource(reader);
        return parseFromInputSource(is);
    }

    /**
     * Generate a document from an input source
     *
     * @param inputSource
     * @return Document
     */
    public static final Document parseFromInputSource(InputSource inputSource) {
        DOMParser parser = new DOMParser();
        Document msg;
        try {
            parser.parse(inputSource);
            msg = parser.getDocument();
        } catch (IOException io) {
            return null;
        } catch (SAXParseException pe) {
            return null;
        } catch (SAXException sx) {
            return null;
        }
        return msg;
    }

    /**
     * Get the text part of an node (such as <Data>foo</Data> would return "foo".
     *
     * @return text part of an element if defined, null if not
     */
    public static final String getText(Node element) {
        NodeList childNodes = element.getChildNodes();
        if (childNodes != null) {
            int count = childNodes.getLength();
            for (int i = 0; i < count; i++) {
                Node node = childNodes.item(i);
                if (node.getNodeType() == Node.TEXT_NODE) {
                    return node.getNodeValue();
                }
            }
        }
        return null;
    }

    public static final Node getFirstElement(Node node, String name) {
        List nodes = getChildren(node, name);
        if (nodes.size() == 0) {
            return null;
        }
        return (Node) nodes.get(0);
    }

    public static final String getFirstElementText(Node nodeIn, String name) {
        Node node = getFirstElement(nodeIn, name);
        if (node != null) {
            return getText(node);
        }

        return null;
    }

    public static final int getFirstElementInt(Node node, String name) {
        String val = getFirstElementText(node, name);
        int ival = 0;
        if (val != null) {
            try {
                ival = Integer.parseInt(val);
            } catch (NumberFormatException nfe) {
                ival = 0;
            }
        }

        return ival;
    }

    public static final String[] getAllElementText(Node node, String name) {
        List nodes = getChildren(node, name);
        String[] strings = new String[nodes.size()];
        if (nodes.size() == 0) {
            return null;
        }
        for (int i = 0; i < nodes.size(); i++) {
            strings[i] = getText((Node) nodes.get(i));
        }

        return strings;
    }

    /**
     * Get all the child elements of this element, for a given name.
     *
     * @param tagName the name of the child elements we're getting.  If null, get all of the child elements
     * @return an array of child nodes, which may be of length zero if there were no listChildren
     */
    public static final List getChildren(Node node, String tagName) {
        NodeList nodes = node.getChildNodes();
        List l = ListUtil.list();
        int tempi = 0;
        for (int ii = 0; ii < nodes.getLength(); ii++) {
            // getChildNodes() returned ALL the child nodes, filter out
            // the ones we don't want
            if (tagName == null || nodes.item(ii).getNodeName().equals(tagName)) {
                l.add(nodes.item(ii));
            }
        }
        return l;
    }

    //-------------------------------------------------------------------------

    /**
     * Return all the child elements directly under this element
     *
     * @return the child elements
     */
    public static final List getAllChildren(Node node) {
        return getChildren(node, null);
    }

    //-------------------------------------------------------------------------
    public static final boolean hasAttribute(Element elem, String attrName) {
        return getAttributeString(elem, attrName) != null;
    }

    //-------------------------------------------------------------------------
    public static final int getAttributeInt(Element elem, String attrName) {
        String val = getAttributeString(elem, attrName);

        int ival = 0;
        if (val != null) {
            try {
                ival = Integer.parseInt(val);
            } catch (NumberFormatException nfe) {
                ival = 0;
            }
        }

        return ival;
    }

    //-------------------------------------------------------------------------
    public static final boolean getAttributeBoolean(Node node, String attrName) {
        String val = getAttributeString(node, attrName);
        return Boolean.valueOf(val).booleanValue();
    }

    //-------------------------------------------------------------------------
    public static final String getAttributeString(Node node, String attrName) {
        Attr aa = getAttribute(node, attrName);
        return (aa == null) ? null : aa.getValue();
    }

    //-------------------------------------------------------------------------
    public static final void setAttribute(Element elem, String attrName, Object value) {
        Attr aa = getAttribute(elem, attrName);
        if (aa != null) {
            aa.setValue(value.toString());
        } else {
            Attr newA = elem.getOwnerDocument().createAttribute(attrName);
            newA.setValue(value.toString());
            elem.setAttributeNode(newA);
        }
    }

    //-------------------------------------------------------------------------

    /**
     * Remove an attribute. Note that if a default value is specified for the attribute, an attribute representing that
     * value will now be present.
     *
     * @param attrName The name of the attribute to remove
     */
    public static final void removeAttribute(Element elem, String attrName) {
        elem.removeAttribute(attrName);
    }

    private static final Attr getAttribute(Node node, String attrName) {
        Attr aa = (Attr) node.getAttributes().getNamedItem(attrName);
        return aa;
    }
}
