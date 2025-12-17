package ht.util.xml;

import ht.util.io.FileUtil;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.StringTokenizer;

/**
 * Copyright (c) 2003 - present HiTorro All rights reserved. User: chris Date: Jan 12, 2005 Time: 4:20:46 PM
 * <p/>
 * General SAX utility methods.
 */
public class SAXUtil {
    private static transient boolean s_saxInitialized = false;

    public static boolean testSax(InputStream is)
            throws IOException, ParserConfigurationException, SAXException {
        return readSax(is, new PrintHandler());
    }

    public static boolean readSax(File f, DefaultHandler handler)
            throws IOException, ParserConfigurationException, SAXException {
        return readSax(FileUtil.getBufferedFileInputStream(f), handler);
    }

    public static boolean readSax(InputStream is, DefaultHandler handler) throws IOException, SAXException, ParserConfigurationException {
        synchronized (SAXUtil.class) {
            if (s_saxInitialized = false) {
                String jaxpPropertyName =
                        "javax.xml.parsers.SAXParserFactory";
                // Pass the parser factory in on the command line with
                // -D to override the use of the Apache parser.
                if (System.getProperty(jaxpPropertyName) == null) {
                    String apacheXercesPropertyValue =
                            "org.apache.xerces.jaxp.SAXParserFactoryImpl";
                    System.setProperty(jaxpPropertyName,
                            apacheXercesPropertyValue);
                }
                s_saxInitialized = true;
            }
        }


        SAXParserFactory factory = SAXParserFactory.newInstance();

        SAXParser parser = factory.newSAXParser();
        parser.parse(is, handler, "UTF-8");
        return true;
    }
}

class PrintHandler extends DefaultHandler {
    private int indentation = 0;

    /**
     * When you see a start tag, print it out and then increase indentation by two spaces. If the element has
     * attributes, place them in parens after the element name.
     */

    public void startElement(String namespaceUri,
                             String localName,
                             String qualifiedName,
                             Attributes attributes) {
        indent(indentation);
        System.out.print("Start tag: " + qualifiedName);
        int numAttributes = attributes.getLength();
        // For <someTag> just print out "someTag". But for
        // <someTag att1="Val1" att2="Val2">, print out
        // "someTag (att1=Val1, att2=Val2).
        if (numAttributes > 0) {
            System.out.print(" (");
            for (int i = 0; i < numAttributes; i++) {
                if (i > 0) {
                    System.out.print(", ");
                }
                System.out.print(attributes.getQName(i) + "=" +
                        attributes.getValue(i));
            }
            System.out.print(")");
        }
        System.out.println();
        indentation = indentation + 2;
    }

    /**
     * When you see the end tag, print it out and decrease indentation level by 2.
     */

    public void endElement(String namespaceUri,
                           String localName,
                           String qualifiedName) {
        indentation = indentation - 2;
        indent(indentation);
        System.out.println("End tag: " + qualifiedName);
    }

    /**
     * Print out the first word of each tag body.
     */

    public void characters(char[] chars,
                           int startIndex,
                           int endIndex) {
        String data = new String(chars, startIndex, endIndex);
        // Whitespace makes up default StringTokenizer delimeters
        StringTokenizer tok = new StringTokenizer(data);
        if (tok.hasMoreTokens()) {
            indent(indentation);
            System.out.print(tok.nextToken());
            if (tok.hasMoreTokens()) {
                System.out.println("...");
            } else {
                System.out.println();
            }
        }
    }

    private void indent(int indentation) {
        for (int i = 0; i < indentation; i++) {
            System.out.print(" ");
        }
    }
}