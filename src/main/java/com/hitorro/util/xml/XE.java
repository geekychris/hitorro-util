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

import com.hitorro.util.core.string.StringUtil;
import org.xml.sax.Attributes;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class XE {
    private XE parent;
    private String name;
    private String value;
    private Attributes attributes;
    private List<XE> children;

    public XE(XE parent, String name, String value, Attributes attr) {
        this.parent = parent;
        this.name = name;
        this.value = value;
        this.attributes = attr;
    }

    public String toString() {
        return name;
    }

    public XE get(String path) {
        String parts[] = StringUtil.tokenizeFromSingleChar(path, ".");
        return get(parts, 0);
    }

    private XE get(String parts[], int index) {
        if (children == null) {
            return null;
        }
        for (XE child : children) {
            if (parts[index].equals(child.getName())) {
                if (index == parts.length - 1) {
                    return child;
                } else {
                    return child.get(parts, index + 1);
                }
            }
        }
        return null;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String val) {
        this.value = val;
    }

    public Attributes getAttributes() {
        return attributes;
    }

    public void addChild(XE child) {
        if (children == null) {
            children = new ArrayList();
        }
        children.add(child);
    }

    public List<XE> getChildren() {
        return children;
    }
}