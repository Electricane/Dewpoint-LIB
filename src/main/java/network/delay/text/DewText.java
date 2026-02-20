package network.delay.text;

public class DewText {

    /**
     * Checks if a string is null or entirely whitespace.
     */
    public static boolean isBlank(String str) {
        return str == null || str.trim().isEmpty();
    }

    /**
     * Converts a byte size into a readable format (for example, 1.5 GB).
     */
    public static String formatSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        char pre = "KMGTPE".charAt(exp - 1);
        return String.format("%.1f %cB", bytes / Math.pow(1024, exp), pre);
    }

    /**
     * Extracts text between two strings.
     * Quick scraping without regex.
     */
    public static String substringBetween(String str, String open, String close) {
        if (str == null || open == null || close == null) return null;
        int start = str.indexOf(open);
        if (start != -1) {
            int end = str.indexOf(close, start + open.length());
            if (end != -1) {
                return str.substring(start + open.length(), end);
            }
        }
        return null;
    }

    /**
     * Checks if a string contains only digits.
     */
    public static boolean isNumeric(String str) {
        if (isBlank(str)) return false;
        return str.chars().allMatch(Character::isDigit);
    }
}
