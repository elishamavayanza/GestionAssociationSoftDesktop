package com.association.util.utils;

import com.association.util.constants.AppConstants;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Locale;

public class NotificationUtil {
    public static boolean sendEmail(String to, String subject, String body) {
        // Implémentation réelle utiliserait JavaMail ou autre service
        System.out.println("Envoi email à: " + to);
        System.out.println("Sujet: " + subject);
        System.out.println("Corps: " + body);
        return true;
    }

    public static String formatCurrencyNotification(BigDecimal amount) {
        // Utilisez Locale.FRENCH pour le format français
        // Et spécifiez la devise (par exemple "CDF" ou "$")
        return MoneyUtil.format(amount, Locale.FRENCH, "CDF");

        // Ou si vous préférez utiliser le format devise par défaut :
        // return MoneyUtil.format(amount, Locale.FRENCH);
    }

    public static String formatDateNotification(Date date) {
        return DateUtil.formatDate(date, AppConstants.DATE_FORMAT);
    }
}