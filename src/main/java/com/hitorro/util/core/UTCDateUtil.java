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

package com.hitorro.util.core;

import com.hitorro.util.core.string.Fmt;
import com.hitorro.util.core.string.StringUtil;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.*;

public class UTCDateUtil {
    public static final TimeZone UTC = TimeZone.getTimeZone("UTC");


    /**
     * the difference, measured in milliseconds, between the current time and midnight, January 1, 1970 UTC.
     * <p/>
     * Note, since the time is in UTC, when translating to and from this space we need to correct the millis by the
     * delta of the time zone verses UTC, else we end up rolling over days....for example:
     * <p/>
     * if the time is 12:30 Feb 14 2007 and we convertToPdf to short date and back to date.  The date returned should be
     * 0:00 Feb 14 2007, but without conversion we get 16:00 Feb 13th.
     */

    private static final long millisOffset = getJan1_1971();

    private static List ValidDateFormats = ListUtil.list();

    /**
     * Get a copy of a Calendar object.
     *
     * @param calend The calendar to copy, if null use now.
     * @return a new Calendar object with exactly the same date as calend
     */
    public static Calendar calendarForCalendar(Calendar calend) {
        Calendar copy = Calendar.getInstance(UTC);
        if (calend != null) {
            copy.setTimeInMillis(calend.getTimeInMillis());
        }

        return copy;
    }

    public static Date getStartOfDay(Date d) {
        java.util.Calendar cal = Calendar.getInstance(UTC);
        cal.setTime(d);

        cal.set(java.util.Calendar.HOUR_OF_DAY, 0);
        cal.set(java.util.Calendar.MINUTE, 0);
        cal.set(java.util.Calendar.SECOND, 0);
        return cal.getTime();
    }

    public static final int[] getYMDAsInArray(Date d) {
        int res[] = new int[3];
        Calendar c = UTCDateUtil.calendarForDate(d);
        res[0] = c.get(Calendar.YEAR);
        res[1] = c.get(Calendar.MONTH) + 1;
        res[2] = c.get(Calendar.DAY_OF_MONTH);
        return res;
    }


    /**
     * Get a calendar object corresponding to a date.
     *
     * @param date the date for which we want a calendar, if null use now
     * @return the calendar object
     */
    public static Calendar calendarForDate(Date date) {
        Calendar calend = Calendar.getInstance(UTC);
        if (date != null) {
            calend.setTime(date);
        }

        return calend;
    }

    /**
     * Snap to sunday morning 00 hrs.  This rounds the time down, so a time of saturday 0912122359 should snap back to
     * 09120600
     *
     * @param date
     * @return
     */
    public static Date snapToWeek(Date date) {
        return snapToWeek(date, Calendar.SUNDAY, 0);
    }

    /**
     * Snap the date to a day hour offset
     *
     * @param date
     * @param dayOffset
     * @param hourOffset
     * @return
     */
    public static Date snapToWeek(Date date, int dayOffset, int hourOffset) {
        Calendar calend = Calendar.getInstance(UTC);
        if (date != null) {
            calend.setTime(date);
        }
        calend.setTimeZone(UTC);
        calend.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
        calend.set(Calendar.HOUR_OF_DAY, 0);
        calend.set(Calendar.MINUTE, 0);
        calend.set(Calendar.SECOND, 0);

        return calend.getTime();
    }

    /**
     * Snap the date to a day hour offset
     *
     * @param date
     * @return
     */
    public static Date snapToDay(Date date) {
        Calendar calend = Calendar.getInstance(UTC);
        if (date != null) {
            calend.setTime(date);
        }
        calend.setTimeZone(UTC);
        calend.set(Calendar.HOUR_OF_DAY, 0);
        calend.set(Calendar.MINUTE, 0);
        calend.set(Calendar.SECOND, 0);

        return calend.getTime();
    }


    /**
     * Change the date (the days value) of a calendar.
     *
     * @param calend The calendar to change in place
     * @param nDays  number of days to offset.  Positive - into the future, negative - into the past.
     */
    public static void changeDays(Calendar calend, int nDays) {
        if (calend != null) {
            calend.set(Calendar.DATE, nDays);
        }
    }


    /**
     * Get a date offset by some number of days.
     *
     * @param date  The date to change, if null use now
     * @param nDays number of days to offset.  Positive - into the future, negative - into the past.
     * @return the resulting offset date.
     */
    public static Date changeDays(Date date, int nDays) {
        Calendar calend = calendarForDate(date);
        changeDays(calend, nDays);
        return calend.getTime();
    }


    /**
     * Set a calendar to midnight.
     *
     * @param calend the calendar object, which will be changed in place.
     */
    public static void changeToMidnight(Calendar calend) {
        if (calend != null) {
            calend.set(Calendar.HOUR, 0);
            calend.set(Calendar.MINUTE, 0);
            calend.set(Calendar.SECOND, 0);
            calend.set(Calendar.MILLISECOND, 0);
        }
    }


    /**
     * Get the "time set to midnight on the same day" version of a date.
     *
     * @param date the date to be turned back to midnight.  If null, we will use the current date.
     * @return a new date, with time at midnight on the same day as date
     */
    public static Date changeToMidnight(Date date) {
        Calendar calend = calendarForDate(date);
        changeToMidnight(calend);

        return calend.getTime();
    }


    /**
     * returns date as a readable integer, such as 20071201 which also is sortable
     *
     * @param y
     * @param m
     * @param dm
     * @return
     */
    public static final int dateAsInt(int y, int m, int dm) {
        int ymdInt = (y * 100) + m;
        ymdInt = (ymdInt * 100) + dm;
        return ymdInt;
    }


    /**
     * Produce an integer of the form <year><month><dayofmonth> from a calender object
     *
     * @param cal
     * @return
     */
    public static final int dateAsIntFromCalendar(Calendar cal) {
        int m = cal.get(Calendar.MONTH) + 1;
        int y = cal.get(Calendar.YEAR);
        int dm = cal.get(Calendar.DAY_OF_MONTH);
        return dateAsInt(y, m, dm);
    }


    /**
     * Produce an integer of the form <year><month><dayofmonth> from a date object
     *
     * @param date
     * @return
     */
    public static final int dateAsIntFromCalendar(Date date) {
        Calendar cal = Calendar.getInstance(UTC);
        cal.setTime(date);
        return dateAsIntFromCalendar(cal);
    }



    /**
     * Produce an integer of the form <year><month><dayofmonth> from a calender object
     *
     * @param cal
     * @return
     */
    public static final String dateAsStringFromCalendar(Calendar cal, String seperator) {
        int m = cal.get(Calendar.MONTH) + 1;
        int y = cal.get(Calendar.YEAR);
        int dm = cal.get(Calendar.DAY_OF_MONTH);
        int ymdInt = (y * 100) + m;
        ymdInt = (ymdInt * 100) + dm;
        return Fmt.S("%s%s%s%s%s", y, seperator, m, seperator, dm);
    }


    public static final String dateAsStringFromCalendar(Date date, String seperator) {
        Calendar cal = Calendar.getInstance(UTC);
        cal.setTime(date);
        return dateAsStringFromCalendar(cal, seperator);
    }


    public static final short dateToShortDate(Date date) {
        return timeMillisToShortDate(date.getTime());
    }


    /**
     * @return Calendar with [One LongWeek before Today] as the Date and [00:00:00] as the Time
     */
    public static java.util.Calendar getCalendarLastWeek() {
        java.util.Calendar lastWeek = Calendar.getInstance(UTC);
        lastWeek.add(java.util.Calendar.DAY_OF_MONTH, -7);
        lastWeek.set(java.util.Calendar.HOUR_OF_DAY, 0);
        lastWeek.set(java.util.Calendar.MINUTE, 0);
        lastWeek.set(java.util.Calendar.SECOND, 0);
        return lastWeek;
    }


    /**
     * @return Calendar with [Date of Legacy Media Migration] as the Date and [00:00:00] as the Time
     */
    public static java.util.Calendar getCalendarLegacyMediaMigration() {
        java.util.Calendar firstWeek = Calendar.getInstance(UTC);
        firstWeek.set(java.util.Calendar.YEAR, 2006);
        firstWeek.set(java.util.Calendar.MONTH, 8);
        firstWeek.set(java.util.Calendar.DAY_OF_MONTH, 26);
        firstWeek.set(java.util.Calendar.HOUR_OF_DAY, 0);
        firstWeek.set(java.util.Calendar.MINUTE, 0);
        firstWeek.set(java.util.Calendar.SECOND, 0);
        return firstWeek;
    }


    /**
     * Get a calendar for the previous named day of the week. * @param dayOfWeek - Calendar Constant (e.g. MONDAY) that
     * is the "previous day" * @param weeksBack - number of weeks back to find the day of the week.<br> 0 is the most
     * recent day back (eg. last MONDAY)<br> 1 for example is a week before that, etc.<br>
     *
     * @return Calendar with [Yesterday] as the Date and [23:59:59] as the Time
     */
    public static java.util.Calendar getCalendarPreviousDayofWeek(int dayOfWeek, int weeksBack) {
        java.util.Calendar cal = Calendar.getInstance(UTC);
        int deltaFromCurrentDayOfWeek;

        deltaFromCurrentDayOfWeek = dayOfWeek - cal.get(Calendar.DAY_OF_WEEK);
        if (deltaFromCurrentDayOfWeek >= 0) {
            deltaFromCurrentDayOfWeek -= 7;
        }
        if (weeksBack > 0) {
            deltaFromCurrentDayOfWeek -= weeksBack * 7;
        }
        cal.add(java.util.Calendar.DAY_OF_MONTH, deltaFromCurrentDayOfWeek);
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0);
        cal.set(java.util.Calendar.MINUTE, 0);
        cal.set(java.util.Calendar.SECOND, 0);
        return cal;
    }

    //*********************************** 16 bit date "stuff" **********************************

    /**
     * @return Calendar with [First LongWeek of Metrics Data] as the Date and [00:00:00] as the Time
     */
    public static java.util.Calendar getCalendarStartDateOfMetrics() {
        java.util.Calendar firstWeek = Calendar.getInstance(UTC);
        firstWeek.set(java.util.Calendar.YEAR, 2006);
        firstWeek.set(java.util.Calendar.MONTH, 5);
        firstWeek.set(java.util.Calendar.DAY_OF_MONTH, 10);
        firstWeek.set(java.util.Calendar.HOUR_OF_DAY, 0);
        firstWeek.set(java.util.Calendar.MINUTE, 0);
        firstWeek.set(java.util.Calendar.SECOND, 0);
        return firstWeek;
    }


    /**
     * @return Calendar with [Yesterday] as the Date and [23:59:59] as the Time
     */
    public static final java.util.Calendar getCalendarYesterday() {
        java.util.Calendar cal = Calendar.getInstance(UTC);
        cal.add(java.util.Calendar.DAY_OF_MONTH, -1);
        cal.set(java.util.Calendar.HOUR_OF_DAY, 23);
        cal.set(java.util.Calendar.MINUTE, 59);
        cal.set(java.util.Calendar.SECOND, 59);
        return cal;
    }


    public static final Date getDate(String date, String dateFormat) {
        return null;
    }

    public static final Date getDateFor(int year, int month, int day, int hour) {
        Calendar outCal = Calendar.getInstance(UTC);
        outCal.set(GregorianCalendar.YEAR, year);
        outCal.set(GregorianCalendar.MONTH, month - 1);
        outCal.set(GregorianCalendar.DAY_OF_MONTH, day);
        outCal.set(GregorianCalendar.HOUR_OF_DAY, hour);
        outCal.set(GregorianCalendar.MINUTE, 0);
        outCal.set(GregorianCalendar.SECOND, 0);

        return outCal.getTime();
    }


    public static final Date getDateFor(int year, int month, int day) {
        Calendar calen = Calendar.getInstance(UTC);
        calen.set(GregorianCalendar.YEAR, year);
        calen.set(GregorianCalendar.MONTH, month - 1);
        calen.set(GregorianCalendar.DAY_OF_MONTH, day);
        return calen.getTime();
    }


    public static final Date getDateForNMinutesFromNow(int mins) {
        Calendar calen = Calendar.getInstance(UTC);

        calen.add(GregorianCalendar.MINUTE, mins);
        return calen.getTime();
    }


    public static final Date getDateForNow() {
        return getDateForNMinutesFromNow(0);
    }


    /**
     * Return epoch as date
     *
     * @return epoch as a date
     */
    public static final Date getEpoch() {
        GregorianCalendar epoch = new GregorianCalendar(1970, 0, 1, 0, 0, 0);
        return epoch.getTime();
    }


    public static Calendar getFirstDayOfMonth(Calendar cal) {
        Calendar result = Calendar.getInstance(UTC);
        result.setTime(cal.getTime());
        int dayOfWeek = result.get(Calendar.MONTH);
        int daysSinceSaturday = 0;
        if (dayOfWeek != Calendar.SATURDAY) {
            daysSinceSaturday = dayOfWeek - Calendar.SUNDAY + 1;
        }
        //int daysSinceSunday = dayOfWeek - Calendar.SUNDAY;
        result.add(Calendar.DAY_OF_MONTH, -1 * daysSinceSaturday);
        return result;
    }


    /**
     * Saturday is the first day of a Metrics week
     *
     * @param cal Calendar for a Date
     * @return Calendar of Saturday of this LongWeek
     */
    public static Calendar getFirstDayOfWeek(Calendar cal) {
        Calendar result = Calendar.getInstance(UTC);
        result.setTime(cal.getTime());
        int dayOfWeek = result.get(Calendar.DAY_OF_WEEK);
        int daysSinceSaturday = 0;
        if (dayOfWeek != Calendar.SATURDAY) {
            daysSinceSaturday = dayOfWeek - Calendar.SUNDAY + 1;
        }
        //int daysSinceSunday = dayOfWeek - Calendar.SUNDAY;
        result.add(Calendar.DAY_OF_MONTH, -1 * daysSinceSaturday);  //  -1 * daysSinceSunday);
        return result;
    }

    public static Date getFirstValidDate(List dateFormats, String valueStr, Date valueDate) {
        for (int i = 0; i < dateFormats.size(); i++) {
            DateFormat dateFormat = (DateFormat) dateFormats.get(i);
            // todo Figure out timezone issues for date searching
            try {
                valueDate = dateFormat.parse(valueStr);
                if (valueDate != null) {
                    // Found one
                    return valueDate;
                }

            } catch (ParseException e) {
                // do nothing
            }
        }
        return null;
    }

    public static Date getFirstValidDate(String valueStr, Date valueDate) {
        return getFirstValidDate(ValidDateFormats, valueStr, valueDate);
    }

    /**
     * Figure out the offset against UTC time that time millis is calculated in.
     *
     * @return
     */
    private static final long getJan1_1971() {
        Calendar cal = Calendar.getInstance(UTC);
        cal.set(java.util.Calendar.YEAR, 1971);
        cal.set(java.util.Calendar.MONTH, 1);
        cal.set(java.util.Calendar.DAY_OF_MONTH, 1);
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0);
        cal.set(java.util.Calendar.MINUTE, 0);
        cal.set(java.util.Calendar.SECOND, 0);
        return cal.getTimeInMillis();
    }


    /**
     * Get the listFiles of format strings that are used to attempt date formatting.
     *
     * @return Date object if parsed or null if a valid format could not be found.
     */
    public static final List getValidDateFormats() {
        return ValidDateFormats;
    }

    /**
     * Set the listFiles of valid date formats used by the system.
     *
     * @param l
     */
    public static final void setValidDateFormats(List l) {
        ValidDateFormats = l;
    }

    /**
     * Converts seconds in seconds to a <code>String</code> in the format HH:mm:ss.
     *
     * @param seconds the seconds as an integer.
     * @return a <code>String</code> representing the seconds in the format HH:mm:ss.
     */
    public static final String secondsIntToString(int seconds, boolean alwaysAddHours) {
        String time;
        StringBuilder secStr = new StringBuilder();
        StringBuilder minStr = new StringBuilder();
        StringBuilder hrsStr = new StringBuilder();

        int sec = seconds % 60;
        int min = (seconds / 60) % 60;
        int hrs = seconds / (60 * 60);

        StringUtil.prependPadToLength(sec, '0', 2, secStr);
        StringUtil.prependPadToLength(min, '0', 2, minStr);
        StringUtil.prependPadToLength(hrs, '0', 2, hrsStr);

        if (alwaysAddHours || hrs > 0) {
            time = Fmt.S("%s:%s:%s", hrsStr, minStr, secStr);
        } else {
            time = Fmt.S("%s:%s", minStr, secStr);
        }

        return time;
    }

    /**
     * Converts to integer seconds a <code>String</code> in the format of HH:mm:ss.
     *
     * @param time the seconds as a string in format HH:mm:ss or mm:ss or ss or sss.
     * @return an <code>int</code> representing the seconds as an integer.
     */
    public static final int secondsStringToInt(String time) {
        String timeTokens[] = StringUtil.tokenizeFromSingleChar(time, ":", true);
        int tokenCnt = timeTokens.length;
        int tokenCntMax = 3;
        int seconds = 0;
        int secondsMultiplier = 1;

        if (!IntegerUtil.isNumber(time.replaceAll("[: ]", ""))) {
            Log.util.error("UTCDateUtil: invalid seconds string to convertToPdf to int: %s", time);
            return 0;
        }

        if (tokenCnt > tokenCntMax) {
            tokenCnt = tokenCntMax;
            Log.util.warn("UTCDateUtil: invalid seconds string for converting to int: %s.  Truncating to hh:mm:ss", time);
        }


        for (int i = tokenCnt - 1; i >= 0; i--) {
            seconds = seconds + (Integer.valueOf(timeTokens[i]).intValue() * secondsMultiplier);
            secondsMultiplier = secondsMultiplier * 60;
        }

        return seconds;
    }

    public static final Date shortDateToDate(short shortDate) {
        return new Date(shortDateToTimeMillis(shortDate));
    }

    /**
     * Convert the short date back into time in millis
     *
     * @param shortDate
     * @return
     */
    public static final long shortDateToTimeMillis(short shortDate) {
        return (shortDate * Constants.MillisInDay) + millisOffset;
    }

    /**
     * Convert millis since jan 1 1970 into days since jan 1971.  This allows us in a short to represent dates upto 1971
     * + 43 years = 2014.  Which since this is used for representing a date in an transient index, I think is fine for
     * now.
     *
     * @param millis
     * @return
     */
    public static final short timeMillisToShortDate(long millis) {
        return (short) ((millis - millisOffset) / Constants.MillisInDay);
    }
}
