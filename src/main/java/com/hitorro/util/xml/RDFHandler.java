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

import com.hitorro.util.core.Console;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

/**
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
