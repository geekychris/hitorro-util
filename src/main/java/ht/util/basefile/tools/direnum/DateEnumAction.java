package ht.util.basefile.tools.direnum;

import ht.util.basefile.fs.BaseFile;

import java.io.IOException;

/**
 * Copyright (c) 2003 - present HiTorro All rights reserved. User: chris Date: Jan 3, 2008 Time: 8:54:06 AM Callback for
 * DirDateEnumerator
 */
public interface DateEnumAction {
    boolean executeDay(int year, int month, int day, BaseFile dayDir) throws IOException;

    boolean executeMonth(int year, int month, BaseFile dayDir) throws IOException;
}