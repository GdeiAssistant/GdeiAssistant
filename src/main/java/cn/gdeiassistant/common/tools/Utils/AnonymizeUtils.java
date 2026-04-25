package cn.gdeiassistant.common.tools.Utils;

/**
 * Replaces internal deleted-account identifiers with a user-friendly label.
 */
public class AnonymizeUtils {

    private static final String DELETED_PREFIX = "del_";
    private static final String ANONYMOUS_LABEL = "已注销用户";

    public static String sanitizeUsername(String username) {
        if (username != null && username.startsWith(DELETED_PREFIX)) {
            return ANONYMOUS_LABEL;
        }
        return username;
    }

    public static String maskUsername(String username) {
        return maskIdentifier(sanitizeUsername(username), 4, 4);
    }

    public static String maskEmail(String email) {
        if (email == null || email.isBlank() || email.contains("*")) {
            return email;
        }
        String[] parts = email.split("@", 2);
        if (parts.length != 2 || parts[0].isBlank()) {
            return maskIdentifier(email, 1, 1);
        }
        return parts[0].charAt(0) + "***@" + parts[1];
    }

    public static String maskPhone(String phone) {
        if (phone == null || phone.isBlank() || phone.contains("*")) {
            return phone;
        }
        String digits = phone.replaceAll("\\s+", "");
        if (digits.length() == 11) {
            return digits.substring(0, 3) + "****" + digits.substring(7);
        }
        if (digits.length() >= 7) {
            return digits.substring(0, 3) + "****" + digits.substring(digits.length() - 2);
        }
        if (digits.length() >= 5) {
            return digits.substring(0, 2) + "***" + digits.substring(digits.length() - 1);
        }
        return "***";
    }

    public static String maskToken(String token) {
        if (token == null || token.isBlank() || token.contains("*")) {
            return token;
        }
        if (token.length() <= 10) {
            return "***";
        }
        return token.substring(0, 6) + "..." + token.substring(token.length() - 4);
    }

    public static String maskAddress(String address) {
        if (address == null || address.isBlank() || address.contains("*")) {
            return address;
        }
        String compact = address.replaceAll("\\s+", "");
        if (compact.length() <= 6) {
            return compact.substring(0, Math.min(2, compact.length())) + "***";
        }
        return compact.substring(0, 6) + "***";
    }

    public static String maskIdentifier(String value, int start, int end) {
        if (value == null || value.isBlank() || value.contains("*")) {
            return value;
        }
        if (value.length() <= 2) {
            return "*";
        }
        int visibleStart = Math.min(start, Math.max(1, value.length() - 1));
        int visibleEnd = Math.min(end, Math.max(0, value.length() - visibleStart - 1));
        if (visibleStart + visibleEnd >= value.length()) {
            return value.charAt(0) + "***";
        }
        return value.substring(0, visibleStart) + "****" + value.substring(value.length() - visibleEnd);
    }
}
