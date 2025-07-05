package com.association.util.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

public class ExchangeRateUtil {
    private static final Map<String, BigDecimal> EXCHANGE_RATES = new HashMap<>();

    static {
        // Taux de change fictifs - à remplacer par des valeurs réelles
        EXCHANGE_RATES.put("USD-CDF", new BigDecimal("2000")); // 1 USD = 2000 CDF
        EXCHANGE_RATES.put("CDF-USD", new BigDecimal("0.0005")); // 1 CDF = 0.0005 USD
    }

    public static BigDecimal convert(BigDecimal amount, String fromCurrency, String toCurrency) {
        if (fromCurrency.equals(toCurrency)) {
            return amount;
        }

        String key = fromCurrency + "-" + toCurrency;
        BigDecimal rate = EXCHANGE_RATES.get(key);
        if (rate == null) {
            throw new IllegalArgumentException("Taux de change non disponible pour " + key);
        }

        return amount.multiply(rate).setScale(2, RoundingMode.HALF_UP);
    }
}