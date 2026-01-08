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
package com.hitorro.util.basefile.tools.direnum;

import com.hitorro.util.basefile.fs.BaseFile;
import com.hitorro.util.core.IntegerUtil;
import com.hitorro.util.core.UTCDateUtil;
import com.hitorro.util.core.opers.HTPredicate;

import java.io.IOException;

/**
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
