package com.project.rpgplugin.util;

public final class StringUtil {

    private StringUtil() {}

    public static String formatDouble(double value) {
        if (value == (long) value) {
            return String.valueOf((long) value);
        }
        return String.format("%.1f", value);
    }

    public static String formatDuration(long millis) {
        if (millis <= 0) return "0s";
        long totalSecs = millis / 1000;
        long minutes = totalSecs / 60;
        long seconds = totalSecs % 60;
        if (minutes > 0) {
            return minutes + "m" + seconds + "s";
        }
        return seconds + "s";
    }
}
