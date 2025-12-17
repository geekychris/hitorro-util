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
package ht.util.versioning;

import java.util.Comparator;

/**
 * Copyright (c) 2003 - present HiTorro All rights reserved.
 * <p/>
 * User: chris
 */
public class OrderVersion implements Comparator<VersionNode> {
    public int compare(VersionNode v, VersionNode v1) {
        return getMajor(v, v1);
    }

    private int getMajor(VersionNode v, VersionNode v1) {
        if (v.getMajor() == v1.getMajor()) {
            return getMinor(v, v1);
        }
        if (v.getMajor() > v1.getMajor()) {
            return -1;
        }
        return 1;
    }

    private int getMinor(VersionNode v, VersionNode v1) {
        if (v.getMinor() == v1.getMinor()) {
            return getPatch(v, v1);
        }
        if (v.getMinor() > v1.getMinor()) {
            return -1;
        }
        return 1;
    }

    private int getPatch(VersionNode v, VersionNode v1) {
        if (v.getPatch() == v1.getPatch()) {
            return getBuild(v, v1);
        }

        if (v.getPatch() > v1.getPatch()) {
            return -1;
        }
        return 1;
    }

    private int getBuild(VersionNode v, VersionNode v1) {
        if (v.getBuildNumber() == v1.getBuildNumber()) {
            return 0;
        }
        if (v.getBuildNumber() > v1.getBuildNumber()) {
            return -1;
        }
        return 1;
    }
}
