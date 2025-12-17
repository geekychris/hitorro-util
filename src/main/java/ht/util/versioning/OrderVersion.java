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
