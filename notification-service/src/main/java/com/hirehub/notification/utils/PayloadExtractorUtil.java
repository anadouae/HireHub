package com.hirehub.notification.utils;

import java.util.Map;

public class PayloadExtractorUtil {

    public static String getString(Map<String, Object> payload, String key) {
        Object value = payload.get(key);
        return value != null ? value.toString() : null;
    }

    public static Long getLong(Map<String, Object> payload, String key) {
        Object value = payload.get(key);
        if (value == null) return null;
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        return Long.parseLong(value.toString());
    }

    public static Integer getInteger(Map<String, Object> payload, String key) {
        Object value = payload.get(key);
        if (value == null) return null;
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return Integer.parseInt(value.toString());
    }

    public static Boolean getBoolean(Map<String, Object> payload, String key) {
        Object value = payload.get(key);
        if (value == null) return null;
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        return Boolean.parseBoolean(value.toString());
    }
}
