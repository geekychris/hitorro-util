package ht.util.io.filedirwatch;

import java.io.File;
import java.util.Comparator;

/**
 * Copyright (c) 2003 - present HiTorro All rights reserved. User: chris Date: Jan 31, 2005 Time: 10:14:55 AM
 */
public class CompareFileLastModified implements Comparator<File> {
    public int compare(File aIn, File bIn) {
        long a = aIn.lastModified();
        long b = bIn.lastModified();
        if (a < b) {
            return -1;
        }
        if (a > b) {
            return 1;
        }
        return 0;
    }

}