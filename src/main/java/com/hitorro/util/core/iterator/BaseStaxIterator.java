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
package com.hitorro.util.core.iterator;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

/**
 * Open up a stax iterator and decode some kind of object from it.
 */
public abstract class BaseStaxIterator<E> extends AbstractIterator<E> {
    protected static XMLInputFactory iFactory = javax.xml.stream.XMLInputFactory.newInstance();
    protected XMLStreamReader reader;
    protected E e = null;
    protected InputStream is;

    public BaseStaxIterator(InputStream is, String encoding) throws XMLStreamException, UnsupportedEncodingException {
        this.is = is;

        reader = iFactory.createXMLStreamReader(new InputStreamReader(is, encoding));
        init();
    }

    public abstract E readNext();

    private void init() throws XMLStreamException {
        if (reader.hasNext()) {
            reader.next();
        }
        e = readNext();
    }

    @Override
    public void close() throws Exception {
        if (is != null) {
            is.close();
            is = null;
        }
    }

    @Override
    public boolean hasNext() {
        return e != null;
    }

    @Override
    public E next() {
        E curr = e;
        e = readNext();
        return curr;
    }

    @Override
    public void remove() {
    }
}
