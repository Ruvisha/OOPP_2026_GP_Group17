package com.fot.ams.util;

import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;

/**
 * Date and Time utilities for formatting and SQL conversions.
 */
public class DateUtil {

    private static final String DATE_FORMAT = "yyyy-MM-dd";
    private static final String TIME_FORMAT = "HH:mm:ss";
    private static final String DISPLAY_DATE_FORMAT = "dd MMM yyyy";
    private static final String DISPLAY_DATETIME_FORMAT = "dd MMM yyyy, hh:mm a";

    private DateUtil() {
    }

    public static Date nowSqlDate() {
        return new Date(System.currentTimeMillis());
    }

    public static Timestamp nowSqlTimestamp() {
        return new Timestamp(System.currentTimeMillis());
    }

    public static String formatDate(java.util.Date date) {
        if (date == null) return "-";
        return new SimpleDateFormat(DATE_FORMAT).format(date);
    }

    public static String formatDisplayDate(java.util.Date date) {
        if (date == null) return "-";
        return new SimpleDateFormat(DISPLAY_DATE_FORMAT).format(date);
    }

    public static String formatDisplayDateTime(java.util.Date date) {
        if (date == null) return "-";
        return new SimpleDateFormat(DISPLAY_DATETIME_FORMAT).format(date);
    }

    public static Date parseDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) return null;
        try {
            java.util.Date parsed = new SimpleDateFormat(DATE_FORMAT).parse(dateStr.trim());
            return new Date(parsed.getTime());
        } catch (ParseException e) {
            return null;
        }
    }

    public static Time parseTime(String timeStr) {
        if (timeStr == null || timeStr.trim().isEmpty()) return null;
        try {
            if (timeStr.trim().length() == 5) {
                timeStr = timeStr.trim() + ":00";
            }
            java.util.Date parsed = new SimpleDateFormat(TIME_FORMAT).parse(timeStr.trim());
            return new Time(parsed.getTime());
        } catch (ParseException e) {
            return null;
        }
    }

    public static String formatTime(Time time) {
        if (time == null) return "-";
        return new SimpleDateFormat("hh:mm a").format(time);
    }
}
