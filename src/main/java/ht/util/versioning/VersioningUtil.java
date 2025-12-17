package ht.util.versioning;

import ht.util.core.string.Fmt;
import ht.util.core.string.StringUtil;

/**
 * Copyright (c) 2003 - present HiTorro All rights reserved. User: chris Date: Oct 27, 2006 Time: 6:59:31 PM Utils
 * associated with major, minor and branch version numbers
 */
public class VersioningUtil {
    /**
     * 1.0 major(1.0 = 2.0 minor(2.0) = 2.1 major(2.1) = 3.0 minor(3.1) = 3.2 branch(3.1) = 3.1.1.0 major(3.1.1.0) =
     * 3.1.2.0 minor(3.1.2.0) = 3.1.2.1 branch(3.1.2.1) = 3.1.2.1.1.0
     *
     * @return
     */
    public static String getMajorVersion(String version) {
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
