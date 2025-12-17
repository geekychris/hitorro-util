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
package com.hitorro.util.json.keys.propaccess;


public class Part {
    protected String name;

    public Part(String name) {
        this.name = name;
    }

    public static Part get(String name, boolean isIndexed) {
        if (isIndexed) {
            return new IndexedPart(name);
        }
        return new Part(name);
    }

    public int compare(Part p) {
        if (isIndexed() != p.isIndexed()) {
            return -1;
        }
        int v = name.compareTo(p.name);
        if (v != 0) {
            return v;
        }
        if (isIndexed()) {
            return getIndexPosition() - p.getIndexPosition();
        }
        return 0;
    }

    public boolean isNull() {
        return false;
    }

    public void setIndex(int index) {

    }

    public String getIndexAsValue() {
        return null;
    }

    public String name() {
        return name;
    }

    public Part clone() {
        Part p = get(name, isIndexed());
        if (isIndexed()) {

            p.setValue(getIndexAsValue());
        }
        return p;
    }

    public String toString() {
        return name;
    }

    public boolean hasName() {
        return name.length() > 0;
    }

    public void setValue(String val) {
    }


    public int getIndexPosition() {
        return -1;
    }

    public boolean isNumeric() {
        return false;
    }

    public boolean isIndexed() {
        return false;
    }

    void append(StringBuilder builder) {
        if (builder.length() > 0) {
            builder.append(".");
        }
        builder.append(name);
    }
}
