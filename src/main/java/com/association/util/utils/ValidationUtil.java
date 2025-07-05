package com.association.util.utils;

import com.association.security.config.PasswordPolicy;
import java.util.Date;
import java.math.BigDecimal;
import java.util.regex.Pattern;

public class ValidationUtil {
    private static final String EMAIL_REGEX = "^[\\w-_.+]*[\\w-_.]@([\\w]+\\.)+[\\w]+[\\w]$";
    private static final String PHONE_REGEX = "^(\\+\\d{1,3}[- ]?)?\\d{10}$";

    public static boolean isValidEmail(String email) {
        return email != null && Pattern.compile(EMAIL_REGEX).matcher(email).matches();
    }

    public static boolean isValidPhone(String phone) {
        return phone != null && Pattern.compile(PHONE_REGEX).matcher(phone).matches();
    }


    public static boolean isStrongPassword(String password, PasswordPolicy policy) {
        if (password == null || policy == null) return false;

        boolean valid = password.length() >= policy.getMinLength();
        if (policy.isRequireUpper()) valid &= !password.equals(password.toLowerCase());
        if (policy.isRequireLower()) valid &= !password.equals(password.toUpperCase());
        if (policy.isRequireDigit()) valid &= password.matches(".*\\d.*");
        if (policy.isRequireSpecial()) valid &= password.matches(".*[!@#$%^&*()].*");

        return valid;
    }

    public static boolean isFutureDate(Date date) {
        return date != null && date.after(new Date());
    }

    public static boolean isPositiveAmount(BigDecimal amount) {
        return amount != null && amount.compareTo(BigDecimal.ZERO) > 0;
    }
}