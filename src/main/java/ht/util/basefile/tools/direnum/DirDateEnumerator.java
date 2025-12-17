package ht.util.basefile.tools.direnum;

import ht.util.basefile.fs.BaseFile;
import ht.util.core.IntegerUtil;
import ht.util.core.UTCDateUtil;
import ht.util.core.opers.HTPredicate;

import java.io.IOException;

/**
 * Copyright (c) 2003 - present HiTorro All rights reserved. User: chris Date: Jan 1, 2008 Time: 9:36:02 AM Given an action
 * to perform, enumerates a file data structure that looks like: <year>/ <month>/ <day>
 */
public class DirDateEnumerator {
    private BaseFile dbDir;

    public DirDateEnumerator(BaseFile dbDir) {
        this.dbDir = dbDir;
    }

    public boolean executeYears(DateEnumAction action, HTPredicate<Integer> oper) throws IOException {
        BaseFile years[] = dbDir.listFiles();
        boolean success = true;
        for (BaseFile y : years) {
            if (!y.isFile()) {
                if (IntegerUtil.isNumber(y.getName())) {
                    int yearInt = Integer.parseInt(y.getName());

                    if (!executeYear(y, yearInt, action, oper)) {
                        success = false;
                    }

                }
            }
        }
        return success;
    }


    public boolean executeYear(BaseFile year, int yearInt,
                               DateEnumAction action,
                               HTPredicate<Integer> oper) throws IOException {

        BaseFile months[] = year.listFiles();
        boolean success = true;
        for (BaseFile m : months) {
            if (!m.isFile()) {
                if (IntegerUtil.isNumber(m.getName())) {
                    int monthInt = Integer.parseInt(m.getName());

                    action.executeMonth(yearInt, monthInt, m);
                    if (!executeMonth(yearInt, m, monthInt, action, oper)) {
                        success = false;
                    }

                }
            }
        }
        return success;
    }

    public boolean executeMonth(int yearInt,
                                BaseFile month,
                                int monthInt,
                                DateEnumAction action,
                                HTPredicate<Integer> oper) throws IOException {
        BaseFile days[] = month.listFiles();
        boolean success = true;

        for (BaseFile d : days) {
            if (!d.isFile()) {
                if (IntegerUtil.isNumber(d.getName())) {
                    int dayInt = Integer.parseInt(d.getName());


                    if (execute(oper, yearInt, monthInt, dayInt)) {
                        if (!action.executeDay(yearInt, monthInt, dayInt, d)) {
                            success = false;
                        }
                    }
                }
            }
        }
        return success;
    }

    private boolean execute(final HTPredicate<Integer> oper, final int yearInt, final int month, final int day) {
        boolean execute = true;
        if (oper != null) {
            int date = UTCDateUtil.dateAsInt(yearInt, month, day);
            if (!oper.test(date)) {
                execute = false;
            }
        }
        return execute;
    }
}
