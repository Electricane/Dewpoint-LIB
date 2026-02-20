package network.delay.env;

import java.lang.management.ManagementFactory;

public class DewSystem {

    /**
     * Returns true if the current OS is Windows.
     */
    public static boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().contains("win");
    }

    /**
     * Returns true if the current OS is macOS.
     */
    public static boolean isMac() {
        return System.getProperty("os.name").toLowerCase().contains("mac");
    }

    /**
     * Returns true if the current OS is Linux.
     */
    public static boolean isLinux() {
        String os = System.getProperty("os.name").toLowerCase();
        return os.contains("nix") || os.contains("nux") || os.contains("aix");
    }

    /**
     * Gets an environment variable as a String.
     * Returns the defaultValue if the variable is not set.
     */
    public static String getEnv(String key, String defaultValue) {
        String val = System.getenv(key);
        return (val == null) ? defaultValue : val;
    }

    /**
     * Gets an environment variable as an integer, with a fallback.
     */
    public static int getEnvInt(String key, int defaultValue) {
        String val = System.getenv(key);
        if (val == null) return defaultValue;
        try {
            return Integer.parseInt(val);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * Gets an environment variable as a boolean.
     * Good for "DEBUG=true" style flags.
     */
    public static boolean getEnvBool(String key, boolean defaultValue) {
        String val = System.getenv(key);
        if (val == null) return defaultValue;
        return Boolean.parseBoolean(val);
    }

    /**
     * Returns true if an environment variable exists, regardless of its value.
     */
    public static boolean hasEnv(String key) {
        return System.getenv(key) != null;
    }

    /**
     * Gets the Process ID (PID) of the current JVM.
     */
    public static long getPid() {
        return ProcessHandle.current().pid();
    }

    /**
     * Returns the JVM uptime in an easily-readable format (e.g., "2m 30s").
     */
    public static String getUptimeString() {
        long uptime = ManagementFactory.getRuntimeMXBean().getUptime();
        long seconds = (uptime / 1000) % 60;
        long minutes = (uptime / (1000 * 60)) % 60;
        long hours = (uptime / (1000 * 60 * 60));
        
        if (hours > 0) return String.format("%dh %dm %ds", hours, minutes, seconds);
        if (minutes > 0) return String.format("%dm %ds", minutes, seconds);
        return seconds + "s";
    }
}