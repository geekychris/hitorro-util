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
package ht.util.xml;

import ht.util.core.iterator.AbstractIterator;

import javax.xml.stream.XMLStreamException;
import java.io.InputStream;

/**
 * Iterator of an XML stream, that based upon some element name will chunk of the xml into domlets that are
 */
public class StaxXMLBaseChainingIterator extends AbstractIterator<XE> {
    private XE curr = null;

    private BaseStaxXmlParser bsParser = new BaseStaxXmlParser();

    public StaxXMLBaseChainingIterator(InputStream is, String elemName) throws XMLStreamException {
        bsParser.setInputStream(is, elemName);
        curr = bsParser.renderObject();
    }

    @Override
    public void close() throws Exception {
        bsParser.close();
    }

    @Override
    public boolean hasNext() {
        return curr != null;
    }

    @Override
    public XE next() {
        XE ret = curr;
        try {
            curr = bsParser.renderObject();
        } catch (XMLStreamException e) {
            return null;
        }
        return ret;
    }

    @Override
    public void remove() {
    }
}
