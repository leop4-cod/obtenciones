package senadi.gob.ec.adminob.util;

public final class AppConfig {

    private AppConfig() {
    }

    public static String get(String propertyKey, String envKey, String defaultValue) {
        String value = System.getProperty(propertyKey);
        if (value == null || value.trim().isEmpty()) {
            value = System.getenv(envKey);
        }
        if (value == null || value.trim().isEmpty()) {
            return defaultValue;
        }
        return value.trim();
    }

    public static boolean isTruthy(String value) {
        return value != null && (
                "true".equalsIgnoreCase(value)
                || "1".equals(value)
                || "yes".equalsIgnoreCase(value)
                || "si".equalsIgnoreCase(value));
    }
}
