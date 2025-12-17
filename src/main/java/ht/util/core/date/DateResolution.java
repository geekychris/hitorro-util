package ht.util.core.date;

import com.fasterxml.jackson.databind.JsonNode;
import ht.util.core.Constants;
import ht.util.core.UTCDateUtil;
import ht.util.core.iterator.Mapper;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Formats dates to different resolutions and also represents the resolution a date is to be measured to The duration is
 * not accurate.  For instance the duration of a month assumes 31 days and the duration of a year is 365.
 */
public enum DateResolution {
    Month("yyyyMM", Constants.MillisInMonth, null),
    Day("yyMMdd", Constants.MillisInDay, null),
    Week("yyww", Constants.MillisInWeek, null),
    Hour("yyMMddHH", Constants.MillisInHour, null),
    Minute("yyMMddHHmm", Constants.MillisInMinute, null),
    Second("yyMMddHHmmss", Constants.MillisInYear, null),
    SecondTZ("yyMMddHHmmssZ", Constants.MillisInYear, Second),
    Year("yyyy", Constants.MillisInYear, null),
    LongMonth("yyyy/MM", Constants.MillisInMonth, Month),
    LongWeek("yyyy/ww", Constants.MillisInWeek, Week),
    LongDay("yyyy/MM/dd", Constants.MillisInDay, Day),
    LongHour("yyyy/MM/dd/HH", Constants.MillisInHour, Hour),
    LongMinute("yyyy/MM/dd/HH/mm", Constants.MillisInMinute, Minute),
    LongSecond("yyyy/MM/dd/HH/mm/ss", Constants.MillisInSecond, Second),
    json("yyyy-MM-dd'T'HH:mm:ss'Z'", Constants.MillisInSecond, Second),
    Spinn3r("yyyy-MM-dd'T'HH:mm:ss'Z'", Constants.MillisInSecond, Second),
    Atom("EEE, dd MMM yyyy HH:mm:ss z", Constants.MillisInSecond, Second);


    protected static ThreadLocal<DateFormat> tls = new ThreadLocal();
    private String pathFormat;
    private DateFormat pathFormatter;
    private long duration;
    private DateResolution canon;
    private Json2DateMapper jsonDateMapper;

    DateResolution(String pathFormat, long duration, DateResolution canon) {
        this.pathFormat = pathFormat;
        pathFormatter = getDF(pathFormat);
        jsonDateMapper = new Json2DateMapper(this);
        this.duration = duration;
        if (canon == null) {
            this.canon = this;
        } else {
            this.canon = canon;
        }
    }

    public static DateResolution getDateResolutionFromMillisRange(long millis) {
        for (DateResolution dr : DateResolution.values()) {
            long drMillis = dr.getDurationInMillis();
            if (drMillis == millis) {
                return dr.getCanonicalResolution();
            }

        }
        return null;
    }

    public Json2DateMapper getJsonDateMapper() {
        return jsonDateMapper;
    }

    private DateFormat getDF(final String pathFormat) {
        DateFormat pf = new SimpleDateFormat(pathFormat);
        pf.setTimeZone(UTCDateUtil.UTC);
        return pf;
    }

    public DateResolution getCanonicalResolution() {
        return canon;
    }

    public boolean isSameResolution(DateResolution res) {
        return duration == res.duration;
    }

    public long getDurationInMillis() {
        return duration;
    }

    public String getNowFormatted() {
        Date d = new Date();
        return getFormatted(d);
    }

    public String getFormatted(Date d) {
        synchronized (pathFormatter) {
            return pathFormatter.format(d);
        }
    }

    public Date parse(String s) throws ParseException {
        synchronized (pathFormatter) {
            return pathFormatter.parse(s);
        }
    }

    public Date parseThreadLocal(String s) throws ParseException {
        DateFormat df = tls.get();
        if (df == null) {
            df = getDF(pathFormat);
            tls.set(df);
        }
        return df.parse(s);
    }

    public DateFormat getPathDateFormatter() {
        return pathFormatter;
    }


}

class Json2DateMapper implements Mapper<JsonNode, Date> {
    private DateResolution dr;

    Json2DateMapper(DateResolution dr) {
        this.dr = dr;
    }

    public Date apply(JsonNode jsonNodes) {
        try {
            return dr.parse(jsonNodes.asText());
        } catch (ParseException e) {
            return null;
        }
    }
}