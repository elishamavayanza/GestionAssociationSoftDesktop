package com.association.util.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

public class ExchangeRateUtil {
    private static final Map<String, BigDecimal> EXCHANGE_RATES = new HashMap<>();

    static {
        EXCHANGE_RATES.put("USD-CDF", new BigDecimal("3000")); // 1 USD = 3000 CDF
        EXCHANGE_RATES.put("CDF-USD", new BigDecimal("0.000333")); // 1 CDF = 0.000333 USD (1/3000)
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