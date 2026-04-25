package com.hirehub.notification.utils;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Objects;

public final class Utils {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final DateTimeFormatter DEFAULT_DATETIME_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm", Locale.FRENCH);

    private Utils() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static String normalizeWhitespace(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ");
    }

    public static String safeTrim(String value) {
        return value == null ? "" : value.trim();
    }

    public static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    public static String firstNonBlank(String... values) {
        if (values == null) {
            return "";
        }
        for (String value : values) {
            if (!isBlank(value)) {
                return value.trim();
            }
        }
        return "";
    }

    public static String generateOtpCode(int length) {
        if (length < 4) {
            throw new IllegalArgumentException("OTP length must be at least 4");
        }
        StringBuilder otp = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            otp.append(RANDOM.nextInt(10));
        }
        return otp.toString();
    }

    public static String formatDateTime(LocalDateTime dateTime) {
        return dateTime == null ? "" : DEFAULT_DATETIME_FORMAT.format(dateTime);
    }

    public static String capitalizeFirstLetter(String value) {
        if (isBlank(value)) {
            return "";
        }
        String trimmed = value.trim();
        if (trimmed.length() == 1) {
            return trimmed.toUpperCase(Locale.ROOT);
        }
        return trimmed.substring(0, 1).toUpperCase(Locale.ROOT) + trimmed.substring(1);
    }

    public static String defaultIfBlank(String value, String fallback) {
        return isBlank(value) ? Objects.toString(fallback, "") : value.trim();
    }


    public static boolean isNegativeOrNull(Long offreId) {
        return offreId == null || offreId < 0;
    }
}
