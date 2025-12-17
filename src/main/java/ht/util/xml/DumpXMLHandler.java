package ht.util.xml;

import ht.util.core.Console;
import ht.util.core.string.StringUtil;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;

/**
 * <p/>
 * Copyright (c) 2003 - present HiTorro All rights reserved. User: chris Date: Sep 26, 2005 Time: 10:02:17 AM
 */
public class DumpXMLHandler extends DefaultHandler {
    int depth = 0;

    public static void main(String args[]) {
        try {
            SAXUtil.readSax(new File("/Users/chris/rssquery"), new DumpXMLHandler());
        } catch (IOException e) {
            Console.println("Exception %s %e", e, e);
        } catch (ParserConfigurationException e) {
            Console.println("Exception %s %e", e, e);
        } catch (SAXException e) {
            Console.println("Exception %s %s %e", e.getMessage(), e.getException(), e);
        }
    }

    /**
     * When you see a start tag, print it out and then increase indentation by two spaces. If the element has
     * attributes, place them in parens after the element name.
     */

    public void startElement(String namespaceUri,
                             String localName,
                             String qualifiedName,
                             Attributes attributes) {
        Console.println("%sSTARTELEMENT: NS:%s LN:%s QN:%s A:%s", StringUtil.padToLength(' ', depth * 4), namespaceUri, localName, qualifiedName, attributes);
        depth++;
    }

    /**
     * When you see the end tag, print it out and decrease indentation level by 2.
     */

    public void endElement(String namespaceUri,
                           String localName,
                           String qualifiedName) {
        depth--;
        Console.println("%sENDELEMENT: NS:%s LN:%s QN:%s", StringUtil.padToLength(' ', depth * 4), namespaceUri, localName, qualifiedName);
    }

    public void set(String title, String description, String topic, String url) {

    }


    /**
     * Print out the first word of each tag body.
     */

    public void characters(char[] chars,
                           int startIndex,
                           int endIndex) {
        Console.println("%sCHARS: %s", StringUtil.padToLength(' ', depth * 4), new String(chars, startIndex, endIndex));
    }
}
