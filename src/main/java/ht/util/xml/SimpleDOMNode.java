/*
    Copyright (c) 2003 - present HiTorro All rights reserved.

*/

package ht.util.xml;

import ht.jsontypesystem.propreaders.JVSProperties;
import ht.util.core.Log;
import ht.util.io.StringInputStream;
import org.w3c.dom.*;
import org.xml.sax.EntityResolver;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;
import java.util.Map;

/**
 * A class to perform straightforward parsing of XML documents
 */
public class SimpleDOMNode {
    private Element m_element;
    private Map m_children;
    private SimpleDOMNode[] m_allChildren;
    private NamedNodeMap m_attributes;

    //-------------------------------------------------------------------------
    public SimpleDOMNode(Element ee) {
        m_element = ee;
        m_children = null;
        m_attributes = null;
    }

    public static SimpleDOMNode getTopNodeFromString(String buffer) {
        InputStream is = new StringInputStream(buffer);
        return getTopNode(is, null);
    }

    //-------------------------------------------------------------------------
    public static SimpleDOMNode getTopNode(String filepath) {
        File infile = new File(filepath);
        return getTopNode(infile);
    }

    public static SimpleDOMNode getTopNode(String filepath, EntityResolver resolver) {
        File infile = new File(filepath);
        return getTopNode(infile, resolver);
    }

    public static SimpleDOMNode getTopNode(File infile) {
        return getTopNode(infile, null);
    }

    //-------------------------------------------------------------------------
    public static SimpleDOMNode getTopNode(File infile, EntityResolver resolver) {
        // get & parse:q the document
        Document doc = null;
        try {
            DocumentBuilderFactory factory =
                    DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            if (resolver != null) {
                builder.setEntityResolver(resolver);
            }
            doc = builder.parse(infile);
        } catch (FactoryConfigurationError e) {
            // unable to get a document builder factory
            Log.util.warn("SimpleDOMDocument: FactoryConfigurationError " + e);
        } catch (ParserConfigurationException e) {
            // parser was unable to be configured
            Log.util.warn("SimpleDOMDocument: ParserConfigurationException " + e);
        } catch (SAXException e) {
            // parsing error
            Log.util.warn("SimpleDOMDocument: SAXException " + e);
        } catch (IOException e) {
            // i/o error
            Log.util.warn("SimpleDOMDocument: IOException " + e);
        }

        // now get the top-level element
        Element tope = doc.getDocumentElement();
        return new SimpleDOMNode(tope);
    }

    public static SimpleDOMNode getTopNode(InputStream inputStream, EntityResolver resolver) {
        // get & parse:q the document
        Document doc = null;
        try {
            DocumentBuilderFactory factory =
                    DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            if (resolver != null) {
                builder.setEntityResolver(resolver);
            }
            doc = builder.parse(inputStream);
        } catch (FactoryConfigurationError e) {
            // unable to get a document builder factory
            Log.util.warn("SimpleDOMDocument: FactoryConfigurationError " + e);
        } catch (ParserConfigurationException e) {
            // parser was unable to be configured
            Log.util.warn("SimpleDOMDocument: ParserConfigurationException " + e);
        } catch (SAXException e) {
            // parsing error
            Log.util.warn("SimpleDOMDocument: SAXException " + e);
        } catch (IOException e) {
            // i/o error
            Log.util.warn("SimpleDOMDocument: IOException " + e);
        }

        // now get the top-level element
        Element tope = doc.getDocumentElement();
        return new SimpleDOMNode(tope);
    }

    //-------------------------------------------------------------------------
    public String getName() {
        return m_element.getTagName();
    }

    /**
     * Get the text underneath this element. We currently just return the first text element.
     *
     * @return The text element of this node.
     */
    public String getText() {
        NodeList childNodes = m_element.getChildNodes();
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

    public SimpleDOMNode getFirstElement(String name) {
        SimpleDOMNode[] nodes = getChildren(name);
        if (nodes.length == 0) {
            return null;
        }
        return nodes[0];
    }

    public String getFirstElementText(String name) {
        SimpleDOMNode node = getFirstElement(name);
        if (node != null) {
            String val = node.getText();
            if (val != null) {
                return JVSProperties.getProperties().resolveJsonVariable(val);
            }
            return val;
        }

        return null;
    }

    public int getFirstElementInt(String name) {
        String val = getFirstElementText(name);
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

    public String[] getAllElementText(String name) {
        SimpleDOMNode[] nodes = getChildren(name);
        String[] strings = new String[nodes.length];
        if (nodes.length == 0) {
            return null;
        }
        for (int i = 0; i < nodes.length; i++) {
            strings[i] = nodes[i].getText();
        }

        return strings;
    }


    //-------------------------------------------------------------------------

    /**
     * Get all the child elements of this element, for a given name.
     *
     * @param tagName the name of the child elements we're getting.  If null, get all of the child elements
     * @return an array of child nodes, which may be of length zero if there were no listChildren
     */
    public SimpleDOMNode[] getChildren(String tagName) {
        if (m_children == null) {
            m_children = new Hashtable();
        }

        SimpleDOMNode[] retval = null;
        if (tagName != null) {
            retval = (SimpleDOMNode[]) m_children.get(tagName);
        }
        if (retval == null) {
            // listChildren aren't in the cache, go get from the element
            NodeList nodes = m_element.getChildNodes();
            SimpleDOMNode[] temp = new SimpleDOMNode[nodes.getLength()];
            int tempi = 0;
            for (int ii = 0; ii < nodes.getLength(); ii++) {
                // getChildNodes() returned ALL the child nodes, filter out
                // the ones we don't want
                if (nodes.item(ii).getNodeType() != Element.ELEMENT_NODE ||
                        (tagName != null && !nodes.item(ii).getNodeName().equals(tagName))) {
                    continue;
                }
                temp[tempi++] = new SimpleDOMNode((Element) nodes.item(ii));
            }
            // reduce the results down to the desired nodes, and save that
            retval = new SimpleDOMNode[tempi];
            System.arraycopy(temp, 0, retval, 0, tempi);

            if (tagName != null) {
                m_children.put(tagName, retval);
            }
        }

        return retval;
    }

    //-------------------------------------------------------------------------

    /**
     * Return all the child elements directly under this element
     *
     * @return the child elements
     */
    public SimpleDOMNode[] getAllChildren() {
        return getChildren(null);
    }

    //-------------------------------------------------------------------------
    public boolean hasAttribute(String attrName) {
        return getAttributeString(attrName) != null;
    }

    //-------------------------------------------------------------------------
    public int getAttributeInt(String attrName) {
        String val = getAttributeString(attrName);

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
    public boolean getAttributeBoolean(String attrName) {
        String val = getAttributeString(attrName);
        return Boolean.valueOf(val).booleanValue();
    }

    //-------------------------------------------------------------------------
    public String getAttributeString(String attrName) {
        if (m_attributes == null) {
            m_attributes = m_element.getAttributes();
        }
        Attr aa = getAttribute(attrName);
        return (aa == null) ? null : aa.getValue();
    }

    //-------------------------------------------------------------------------
    public void setAttribute(String attrName, Object value) {
        Attr aa = getAttribute(attrName);
        if (aa != null) {
            aa.setValue(value.toString());
        } else {
            Attr newA = m_element.getOwnerDocument().createAttribute(attrName);
            newA.setValue(value.toString());
            m_element.setAttributeNode(newA);
            m_attributes = null; // since our cached values are now incorrect
        }
    }

    //-------------------------------------------------------------------------

    /**
     * Remove an attribute. Note that if a default value is specified for the attribute, an attribute representing that
     * value will now be present.
     *
     * @param attrName The name of the attribute to remove
     */
    public void removeAttribute(String attrName) {
        m_element.removeAttribute(attrName);
        m_attributes = null;  // since our cached values are now incorrect
    }

    //-------------------------------------------------------------------------
    private Attr getAttribute(String attrName) {
        if (m_attributes == null) {
            m_attributes = m_element.getAttributes();
        }
        Attr aa = (Attr) m_attributes.getNamedItem(attrName);
        return aa;
    }

}
