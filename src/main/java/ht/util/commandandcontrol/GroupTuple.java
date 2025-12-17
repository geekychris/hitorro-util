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
package ht.util.commandandcontrol;

import org.xml.sax.helpers.AttributesImpl;

/**
 * <p/>
 * Copyright (c) 2003 - present HiTorro All rights reserved. User: chris Date: Aug 2, 2005 Time: 10:15:59 PM Defines the
 * offset within the table to apply fields (in an multi row response)
 */
public class GroupTuple {
    String setName;
    String name;
    String shortName;
    Class type;
    int size;
    AttributesImpl attribute;

    public GroupTuple(String setName, String nameIn, String shortNameIn, Class typeIn, int sizeIn) {
        this.setName = setName;
        name = nameIn;
        shortName = shortNameIn;
        type = typeIn;
        size = sizeIn;
        attribute = new AttributesImpl();
        attribute.addAttribute("", "name", "", "", shortName);
    }

    public int getSize() {
        return size;
    }

    public String getSetName() {
        return setName;
    }

    public String getName() {
        return name;
    }

    public String getShortName() {
        return shortName;
    }

    public Class getTupleType() {
        return type;
    }
}