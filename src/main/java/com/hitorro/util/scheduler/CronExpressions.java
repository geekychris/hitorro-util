/*
 * Copyright (c) 2006-2026 Chris Collins
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
package com.hitorro.util.scheduler;

import org.quartz.CronExpression;

import java.text.ParseException;
import java.util.Date;

/** Helpers for building and validating Quartz cron expressions. */
public final class CronExpressions {

    private CronExpressions() {}

    // Common cron literals — 7-field Quartz form (sec min hour day-of-month month day-of-week).
    public static final String EVERY_MINUTE   = "0 * * * * ?";
    public static final String EVERY_5_MIN    = "0 */5 * * * ?";
    public static final String EVERY_HOUR     = "0 0 * * * ?";
    public static final String DAILY_MIDNIGHT = "0 0 0 * * ?";
    public static final String WEEKLY_MONDAY  = "0 0 0 ? * MON";

    /** Returns true if the expression parses; false otherwise. */
    public static boolean isValid(String expr) {
        return CronExpression.isValidExpression(expr);
    }

    /** Returns the next fire time after {@code from}, or null if the expression never fires again. */
    public static Date nextAfter(String expr, Date from) {
        try {
            return new CronExpression(expr).getNextValidTimeAfter(from);
        } catch (ParseException e) {
            throw new IllegalArgumentException("invalid cron expression: " + expr, e);
        }
    }

    public static void requireValid(String expr) {
        if (!isValid(expr)) {
            throw new IllegalArgumentException("invalid cron expression: " + expr);
        }
    }
}
