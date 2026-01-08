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
import com.hitorro.util.core.UTCDateUtil;
import com.hitorro.util.core.opers.HTPredicate;
import com.hitorro.util.core.opers.IntegerOperator;
import com.hitorro.util.core.opers.LogicalAndOperator;
import com.hitorro.util.core.string.Fmt;

/**
 */
public class DirEnumUtil {

    public static final int JanToMarch = 0;
    public static final int AprilToJune = 1;
    public static final int JulyToSept = 2;
    public static final int OctoberToDecember = 3;
    public static final int Q1 = JanToMarch;
    public static final int Q2 = AprilToJune;
    public static final int Q3 = JulyToSept;
    public static final int Q4 = OctoberToDecember;

    /**
     * File that represents a quarter of a year, such as:
     * <p/>
     * <root>/2007/Q1 which would be called with quarter=0
     *
     * @param rootDir
     * @param year
     * @param quarter starting at 0
     * @return
     */
    public static final BaseFile getDirForQuarter(BaseFile rootDir, int year, int quarter) {
        return rootDir.getChild(Fmt.S("%s/Q%s", year, quarter + 1));
    }

    public static final BaseFile getDirForMonth(BaseFile rootDir, int year, int month) {
        return rootDir.getChild(Fmt.S("%s/%s", year, month));
    }

    public static final BaseFile getDirForDay(BaseFile rootDir, int year, int month, int day) {
        return rootDir.getChild(Fmt.S("%s/%s/%s", year, month, day));
    }

    public static final BaseFile getDirForYear(BaseFile rootDir, int year) {
        return rootDir.getChild(Fmt.S("%s", year));
    }

    public static final HTPredicate<Integer> getConstraintRange(int ys, int ms, int ds, int ye, int me, int de) {
        int dateStart = UTCDateUtil.dateAsInt(ys, ms, ds);
        int dateEnd = UTCDateUtil.dateAsInt(ye, me, de);
        return new LogicalAndOperator<Integer>(new IntegerOperator(IntegerOperator.GreaterThanOrEqual, dateStart),
                new IntegerOperator(IntegerOperator.LessThanOrEqual, dateEnd));
    }

    public static final HTPredicate<Integer> getMonthof(int ys, int ms) {
        int dateStart = UTCDateUtil.dateAsInt(ys, ms, 1);
        int dateEnd = UTCDateUtil.dateAsInt(ys, ms, 31);
        return new LogicalAndOperator<Integer>(new IntegerOperator(IntegerOperator.GreaterThanOrEqual, dateStart),
                new IntegerOperator(IntegerOperator.LessThanOrEqual, dateEnd));
    }

    /**
     * constraint of a quarter, quarters are:
     *
     * @param ys
     * @param quarter
     * @return
     */
    public static final HTPredicate<Integer> getQuarterOf(int ys, int quarter) {
        int dateStart = UTCDateUtil.dateAsInt(ys, 1 + (3 * quarter), 1);
        int dateEnd = UTCDateUtil.dateAsInt(ys, 3 + (3 * quarter), 31);
        return new LogicalAndOperator<Integer>(new IntegerOperator(IntegerOperator.GreaterThanOrEqual, dateStart),
                new IntegerOperator(IntegerOperator.LessThanOrEqual, dateEnd));
    }
}
