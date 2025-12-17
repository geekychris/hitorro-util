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
import ht.util.core.iterator.mappers.BaseMapper;
import ht.util.excelaccess.Log;

import javax.xml.stream.XMLStreamException;
import java.io.InputStream;

/**
 *
 */
public class XEDomMapper extends BaseMapper<InputStream, AbstractIterator<XE>> {
    private String path;

    public XEDomMapper(String path) {
        this.path = path;
    }

    public AbstractIterator<XE> apply(InputStream is) {
        try {
            return new StaxXMLBaseChainingIterator(is, path);
        } catch (XMLStreamException e) {
            Log.util.error("%s %e", e, e);
            return null;
        }
    }
}

