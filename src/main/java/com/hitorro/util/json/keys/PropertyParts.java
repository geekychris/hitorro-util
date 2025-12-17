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
package com.hitorro.util.json.keys;


import com.hitorro.util.core.ArrayUtil;
import com.hitorro.util.core.string.StringUtil;

/**
 *
 */
public class PropertyParts {
    private String[] parts;
    private String path;
    private boolean[] isNumber;

    public PropertyParts(String path) {
        setPath(path);
    }

    public boolean lastPartIndexed() {
        return isNumber[isNumber.length - 1];
    }

    public String getName() {
        if (lastPartIndexed()) {
            return parts[parts.length - 2];
        }
        return parts[parts.length - 1];
    }


    public int getIndex() {
        if (lastPartIndexed()) {
            return Integer.parseInt(parts[parts.length - 1]);
        }
        return -1;
    }

    private void computeParts(String path) throws PropertyException {
        if (!StringUtil.ensureBalance(path, '[', ']')) {
            throw new PropertyException("Unbalanced ");
        }
        int count = StringUtil.countInstances(path, '[');
        String p[] = path.split("\\.");
        if (ArrayUtil.nullOrEmpty(p)) {
            p = new String[1];
            p[0] = path;
        }
        int total = p.length + count;
        parts = new String[total];
        isNumber = new boolean[total];
        int ind = 0;
        for (int i = 0; i < p.length; i++) {
            int index = p[i].indexOf('[');
            if (index == -1) {
                parts[ind++] = p[i];
            } else {
                String s = p[i];
                parts[ind++] = s.substring(0, index);
                parts[ind] = s.substring(index + 1, s.length() - 1);
                isNumber[ind++] = true;
            }
        }
    }

    public String[] getParts() {
        return parts;
    }

    public boolean[] getNumber() {
        return isNumber;
    }

    public String getPath() {
        return path;
    }

    /**
     * Allow redefinition of the path
     *
     * @param path
     */
    public void setPath(String path) {
        computeParts(path);
        this.path = path;
    }
}
