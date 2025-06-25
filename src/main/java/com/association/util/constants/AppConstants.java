package com.association.util.constants;

import com.association.util.constants.DatePattern;
import java.math.BigDecimal;
import java.util.Locale;

public class AppConstants {
    public static final BigDecimal MAX_EMPRUNT = new BigDecimal("500000");
    public static final BigDecimal MIN_CONTRIBUTION = new BigDecimal("1000");
    public static final int MAX_EMPRUNT_DUREE = 12;
    public static final double TAUX_INTERET = 0.05;
    public static final DatePattern DATE_FORMAT = DatePattern.FRENCH_DATE;
    public static final Locale DEFAULT_LOCALE = Locale.FRANCE;
}