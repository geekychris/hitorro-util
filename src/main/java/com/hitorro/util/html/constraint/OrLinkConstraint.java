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
package com.hitorro.util.html.constraint;

import com.hitorro.util.html.Link;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import java.net.URL;

/**
 */
public class OrLinkConstraint extends LogicalLinkConstraint {
    public OrLinkConstraint(LinkConstraint... constraints) {
        super(constraints);
    }

    public boolean match(String url, Link.LinkType type, String title, String typeString, Document doc, Node elem, URL sourceUrl) {
        for (LinkConstraint c : m_constraints) {
            if (c.match(url, type, title, typeString, doc, elem, sourceUrl)) {
                return true;
            }
        }
        return false;
    }
}
