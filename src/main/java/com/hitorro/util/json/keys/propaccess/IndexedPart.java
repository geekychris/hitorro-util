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

import com.hitorro.util.core.IntegerUtil;
import com.hitorro.util.core.string.Fmt;

public class IndexedPart extends Part {
    private String val;
    private boolean isNum;
    private int index;

    public IndexedPart(final String name) {
        super(name);
    }

    public void setIndex(int index) {
        this.index = index;
        this.isNum = true;
    }

    public Part clone() {
        IndexedPart i = new IndexedPart(name);
        i.val = val;
        i.isNum = isNum;
        i.index = index;
        return i;
    }

    public void setValue(String val) {
        this.val = val;
        isNum = IntegerUtil.isNumber(val);
        if (isNum) {
            index = IntegerUtil.parseInt(val);
        }
    }

    public String getIndexAsValue() {
        return val;
    }

    public int getIndexPosition() {
        return index;
    }

    public boolean isNumeric() {
        return isNum;
    }

    public boolean isIndexed() {
        return true;
    }

    public boolean isNull() {
        if (isNum) {
            return false;
        }
        return val == null || val.length() == 0;
    }

    void append(StringBuilder builder) {
        super.append(builder);
        builder.append('[');
        if (isNum) {
            builder.append(index);
        } else {
            builder.append(val);
        }

        builder.append(']');
    }

    public String toString() {
        if (isNum) {
            return Fmt.S("%s[%s]", name, index);
        }
        return Fmt.S("%s[%s]", name, val);
    }
}
