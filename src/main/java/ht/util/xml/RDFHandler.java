package ht.util.xml;

import ht.util.core.Console;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Copyright (c) 2003 - present HiTorro All rights reserved. User: chris Date: Jan 12, 2005 Time: 5:20:53 PM
 */
public class RDFHandler extends DefaultHandler {
    public static final String ExternalPage = "ExternalPage";
    public static final String ExternalPageTitle = "d:Title";
    public static final String ExternalPageDescription = "d:Description";
    public static final String ExternalPageTopic = "topic";

    private boolean m_inEP = false;
    private String m_title;
    private String m_description;
    private String m_topic;
    private String m_about;
    private int m_counter = 0;
    private StringBuilder m_builder = new StringBuilder();

    /**
     * When you see a start tag, print it out and then increase indentation by two spaces. If the element has
     * attributes, place them in parens after the element name.
     */

    public void startElement(String namespaceUri,
                             String localName,
                             String qualifiedName,
                             Attributes attributes) {
        if (qualifiedName.equals(ExternalPage)) {
            m_inEP = true;

            m_title = null;
            m_description = null;
            m_topic = null;
            m_about = null;
            // attributes processing here.
            int numAttributes = attributes.getLength();
            if (numAttributes > 0) {
                for (int i = 0; i < numAttributes; i++) {
                    String name = attributes.getQName(i);
                    if (name.equals("about")) {
                        m_about = attributes.getValue(i);
                    }

                }
            }
            return;
        }

    }

    /**
     * When you see the end tag, print it out and decrease indentation level by 2.
     */

    public void endElement(String namespaceUri,
                           String localName,
                           String qualifiedName) {
        if (m_inEP) {
            if (qualifiedName.equals(ExternalPageTitle)) {
                m_title = m_builder.toString();
            } else if (qualifiedName.equals(ExternalPageDescription)) {
                m_description = m_builder.toString();
            } else if (qualifiedName.equals(ExternalPageTopic)) {
                m_topic = m_builder.toString();
            } else if (qualifiedName.equals(ExternalPage)) {
                m_inEP = false;
                m_counter++;
                if (m_counter % 1000 == 0) {
                    Console.print("%s,", m_counter);
                    if (m_counter % 10000 == 0) {
                        Console.println();
                    }
                }
                set(m_title, m_description, m_topic, m_about);
                //Console.println("Title: %s >>>> Description: %s >>> Topic : %s >>>> About: %s", m_title, m_description, m_topic, m_about);
                return;
            }
            m_builder.setLength(0);
        }
    }

    public void set(String title, String description, String topic, String url) {

    }

    /**
     * Print out the first word of each tag body.
     */

    public void characters(char[] chars,
                           int startIndex,
                           int endIndex) {
        if (m_inEP) {
            m_builder.append(chars, startIndex, endIndex);
        }

    }
}
