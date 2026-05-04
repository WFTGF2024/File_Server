package com.example.fileserver.common.utils;

public class FileSizeUtil {

    private static final long KB = 1024;
    private static final long MB = KB * 1024;
    private static final long GB = MB * 1024;
    private static final long TB = GB * 1024;

    private FileSizeUtil() {
    }

    public static String format(long bytes) {
        if (bytes >= TB) {
            return String.format("%.2f TB", (double) bytes / TB);
        } else if (bytes >= GB) {
            return String.format("%.2f GB", (double) bytes / GB);
        } else if (bytes >= MB) {
            return String.format("%.2f MB", (double) bytes / MB);
        } else if (bytes >= KB) {
            return String.format("%.2f KB", (double) bytes / KB);
        } else {
            return bytes + " B";
        }
    }

    public static long parseToBytes(String sizeStr) {
        if (sizeStr == null || sizeStr.isBlank()) {
            return 0;
        }
        sizeStr = sizeStr.trim().toUpperCase();
        try {
            if (sizeStr.endsWith("TB")) {
                return (long) (Double.parseDouble(sizeStr.replace("TB", "")) * TB);
            } else if (sizeStr.endsWith("GB")) {
                return (long) (Double.parseDouble(sizeStr.replace("GB", "")) * GB);
            } else if (sizeStr.endsWith("MB")) {
                return (long) (Double.parseDouble(sizeStr.replace("MB", "")) * MB);
            } else if (sizeStr.endsWith("KB")) {
                return (long) (Double.parseDouble(sizeStr.replace("KB", "")) * KB);
            } else {
                return Long.parseLong(sizeStr);
            }
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
