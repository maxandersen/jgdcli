package dk.xam.jgdcli.cli;

import java.util.List;

public class OutputFormatter {

    public static void printTable(String[] headers, List<String[]> rows) {
        System.out.println(String.join("\t", headers));
        for (String[] row : rows) {
            System.out.println(String.join("\t", row));
        }
    }

    public static void printKeyValue(String key, String value) {
        System.out.println(key + ": " + (value != null ? value : "-"));
    }

    public static void printKeyValue(String key, Object value) {
        printKeyValue(key, value != null ? value.toString() : null);
    }

    public static String orEmpty(String value) {
        return value != null ? value : "";
    }

    public static String orDefault(String value, String defaultValue) {
        return value != null ? value : defaultValue;
    }

    public static String formatSize(Long bytes) {
        if (bytes == null || bytes == 0) return "-";
        String[] units = {"B", "KB", "MB", "GB", "TB"};
        int i = (int) Math.floor(Math.log(bytes) / Math.log(1024));
        i = Math.min(i, units.length - 1);
        double size = bytes / Math.pow(1024, i);
        return i > 0 ? String.format("%.1f %s", size, units[i]) : String.format("%.0f %s", size, units[i]);
    }

    public static String formatSize(String bytesStr) {
        if (bytesStr == null || bytesStr.isEmpty()) return "-";
        try {
            return formatSize(Long.parseLong(bytesStr));
        } catch (NumberFormatException e) {
            return "-";
        }
    }

    public static String sanitize(String value) {
        return value != null ? value.replace("\t", " ").replace("\n", " ").replace("\r", "") : "";
    }
}
