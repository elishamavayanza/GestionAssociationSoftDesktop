package com.association.util.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Optional;

public class MoneyUtil {
    public static String format(BigDecimal montant, Locale locale) {
        if (montant == null) return "";
        return NumberFormat.getCurrencyInstance(locale).format(montant);
    }

    public static String format(BigDecimal montant) {
        return format(montant, Locale.FRENCH);
    }

    public static Optional<BigDecimal> parse(String montantStr, Locale locale) {
        try {
            Number number = NumberFormat.getNumberInstance(locale).parse(montantStr);
            return Optional.of(new BigDecimal(number.toString()));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public static BigDecimal round(BigDecimal amount, int scale) {
        if (amount == null) return BigDecimal.ZERO;
        return amount.setScale(scale, RoundingMode.HALF_UP);
    }

    public static BigDecimal round(BigDecimal amount) {
        return round(amount, 2);
    }
}