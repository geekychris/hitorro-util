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
import com.hitorro.util.core.string.StringUtil;
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
