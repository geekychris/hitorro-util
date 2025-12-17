package ht.util.basefile.tools.direnum;

import ht.util.basefile.fs.BaseFile;
import ht.util.core.Console;

/**
 * Copyright (c) 2003 - present HiTorro All rights reserved. User: chris Date: Jan 3, 2008 Time: 9:34:21 AM Test action to
 * print out the dates executed
 */
public class PrintAction implements DateEnumAction {


    public boolean executeDay(int year, int month, int day, BaseFile dayDir) {
        Console.println("execDay: %s %s %s", year, month, day);
        return true;
    }

    public boolean executeMonth(int year, int month, BaseFile dayDir) {
        Console.println("execMonth: %s %s", year, month);
        return true;
    }
}
