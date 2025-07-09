package com.association.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ImportUtils {
    private static final SimpleDateFormat[] DATE_FORMATS = {
            new SimpleDateFormat("dd/MM/yyyy"),
            new SimpleDateFormat("yyyy-MM-dd"),
            new SimpleDateFormat("MM/dd/yyyy")
    };

    public static Date parseDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) {
            return null;
        }

        for (SimpleDateFormat format : DATE_FORMATS) {
            try {
                return format.parse(dateStr);
            } catch (ParseException ignored) {
                // Essayer le format suivant
            }
        }
        return null;
    }

    public static String formatDate(Date date) {
        if (date == null) {
            return "";
        }
        return DATE_FORMATS[0].format(date);
    }
}