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
package com.hitorro.util.versioning;

import com.hitorro.util.core.string.Fmt;
import com.hitorro.util.core.string.StringUtil;

/**
 * associated with major, minor and branch version numbers
 */
public class VersioningUtil {
    /**
     * 1.0 major(1.0 = 2.0 minor(2.0) = 2.1 major(2.1) = 3.0 minor(3.1) = 3.2 branch(3.1) = 3.1.1.0 major(3.1.1.0) =
     * 3.1.2.0 minor(3.1.2.0) = 3.1.2.1 branch(3.1.2.1) = 3.1.2.1.1.0
     *
     * @return
     */
    public static final String getMajorVersion(String version) {
        String leftPart = getLeft(version);
        int major = getMajorAsInt(version);
        if (StringUtil.nullOrEmptyOrBlankString(leftPart)) {
            return Fmt.S("%s.0", Integer.toString(major + 1));
        } else {
            return Fmt.S("%s.%s.0", leftPart, Integer.toString(major + 1));
        }
    }

    public static final String getMinorVersion(String version) {
        String leftPart = getLeft(version);
        String major = getMajorAsString(version);
        int minor = getMinorAsInt(version);
        if (StringUtil.nullOrEmptyOrBlankString(leftPart)) {
            return Fmt.S("%s.%s", major, Integer.toString(minor + 1));
        } else {
            return Fmt.S("%s.%s.%s", leftPart, major, Integer.toString(minor + 1));
        }
    }

    public static final String getBranch(String version) {
        return Fmt.S("%s.1.0", version);
    }

    protected static final int getMajorAsInt(String version) {
        String s = getMajorAsString(version);
        if (s == null) {
            return -1000;
        }
        return Integer.parseInt(s);
    }

    protected static final int getMinorAsInt(String version) {
        String s = getMinorAsString(version);
        if (s == null) {
            return -1000;
        }
        return Integer.parseInt(s);
    }

    protected static final String getMajorAsString(String version) {
        int index = indexOfBackwards(version, '.', version.length() - 1);
        if (index == -1) {
            return null;
        }
        int i2 = indexOfBackwards(version, '.', index - 1);
        if (i2 == -1) {
            return version.substring(0, index);
        }
        return version.substring(i2 + 1, index);
    }

    protected static final String getMinorAsString(String version) {
        int index = indexOfBackwards(version, '.', version.length() - 1);
        if (index == -1) {
            return null;
        }
        return version.substring(index + 1);
    }

    protected static final int indexOfBackwards(String s, char t, int index) {
        for (int i = index; i >= 0; i--) {
            if (s.charAt(i) == t) {
                return i;
            }
        }
        return -1;
    }

    protected static final String getLeft(String version) {
        int index = indexOfBackwards(version, '.', version.length() - 1);
        if (index == -1) {
            return null;
        }
        int i2 = indexOfBackwards(version, '.', index - 1);
        if (i2 == -1) {
            return null;
        }
        return version.substring(0, i2);
    }
}
