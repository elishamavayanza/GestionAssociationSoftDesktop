package com.association.util.utils;

import com.association.util.constants.DatePattern;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.Optional;
import java.util.Calendar;

public class DateUtil {

    public static String formatDate(Date date, DatePattern pattern) {
        if (date == null) return "";
        return new SimpleDateFormat(pattern.getPattern()).format(date);
    }

    public static Optional<Date> parseDate(String dateStr, DatePattern pattern) {
        try {
            return Optional.of(new SimpleDateFormat(pattern.getPattern()).parse(dateStr));
        } catch (ParseException e) {
            return Optional.empty();
        }
    }

    public static Date addDays(Date date, int days) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DAY_OF_YEAR, days);
        return calendar.getTime();
    }

    public static long daysBetween(Date start, Date end) {
        long diff = end.getTime() - start.getTime();
        return diff / (1000 * 60 * 60 * 24);
    }

    public static boolean isBetween(Date date, Date start, Date end) {
        return !date.before(start) && !date.after(end);
    }

    // Nouvelles méthodes pour la conversion vers LocalDate
    public static LocalDate toLocalDate(Date date) {
        if (date == null) return null;

        if (date instanceof java.sql.Date) {
            return ((java.sql.Date) date).toLocalDate();
        }
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    public static Date toDate(LocalDate localDate) {
        if (localDate == null) return null;
        return java.sql.Date.valueOf(localDate);
    }

    // Méthode spécifique pour java.sql.Date
    public static java.sql.Date toSqlDate(Date date) {
        if (date == null) return null;
        return new java.sql.Date(date.getTime());
    }

    // Méthode pour vérifier si une date est un java.sql.Date
    public static boolean isSqlDate(Date date) {
        return date instanceof java.sql.Date;
    }
}