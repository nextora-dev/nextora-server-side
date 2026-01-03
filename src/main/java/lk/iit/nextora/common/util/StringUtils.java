package lk.iit.nextora.common.util;

import java.security.SecureRandom;
import java.text.Normalizer;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Utility class for string operations.
 * All methods are static - no instantiation needed.
 *
 * Usage:
 * <pre>
 * boolean empty = StringUtils.isEmpty(str);
 * String slug = StringUtils.toSlug("Hello World");
 * String masked = StringUtils.maskEmail("john@example.com");
 * </pre>
 */
public final class StringUtils {

    private StringUtils() {
        throw new IllegalStateException("Utility class - cannot be instantiated");
    }

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String ALPHANUMERIC = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final String NUMERIC = "0123456789";

    // ==================== Null/Empty Checks ====================

    public static boolean isEmpty(String str) {
        return str == null || str.isEmpty();
    }

    public static boolean isBlank(String str) {
        return str == null || str.isBlank();
    }

    public static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }

    public static boolean isNotBlank(String str) {
        return !isBlank(str);
    }

    public static String defaultIfEmpty(String str, String defaultValue) {
        return isEmpty(str) ? defaultValue : str;
    }

    public static String defaultIfBlank(String str, String defaultValue) {
        return isBlank(str) ? defaultValue : str;
    }

    // ==================== Trimming ====================

    public static String trim(String str) {
        return str != null ? str.trim() : null;
    }

    public static String trimToNull(String str) {
        String trimmed = trim(str);
        return isBlank(trimmed) ? null : trimmed;
    }

    public static String trimToEmpty(String str) {
        return str != null ? str.trim() : "";
    }

    // ==================== Case Conversion ====================

    public static String capitalize(String str) {
        if (isBlank(str)) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }

    public static String capitalizeWords(String str) {
        if (isBlank(str)) return str;
        String[] words = str.split("\\s+");
        StringBuilder result = new StringBuilder();
        for (String word : words) {
            if (result.length() > 0) result.append(" ");
            result.append(capitalize(word));
        }
        return result.toString();
    }

    public static String toCamelCase(String str) {
        if (isBlank(str)) return str;
        String[] parts = str.split("[\\s_-]+");
        StringBuilder result = new StringBuilder(parts[0].toLowerCase());
        for (int i = 1; i < parts.length; i++) {
            result.append(capitalize(parts[i]));
        }
        return result.toString();
    }

    public static String toSnakeCase(String str) {
        if (isBlank(str)) return str;
        return str.replaceAll("([a-z])([A-Z])", "$1_$2")
                  .replaceAll("[\\s-]+", "_")
                  .toLowerCase();
    }

    public static String toKebabCase(String str) {
        if (isBlank(str)) return str;
        return str.replaceAll("([a-z])([A-Z])", "$1-$2")
                  .replaceAll("[\\s_]+", "-")
                  .toLowerCase();
    }

    // ==================== Slug Generation ====================

    public static String toSlug(String str) {
        if (isBlank(str)) return "";
        String normalized = Normalizer.normalize(str, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        return pattern.matcher(normalized)
                .replaceAll("")
                .toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("[\\s-]+", "-")
                .replaceAll("^-|-$", "");
    }

    // ==================== Random String Generation ====================

    public static String randomAlphanumeric(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(ALPHANUMERIC.charAt(RANDOM.nextInt(ALPHANUMERIC.length())));
        }
        return sb.toString();
    }

    public static String randomNumeric(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(NUMERIC.charAt(RANDOM.nextInt(NUMERIC.length())));
        }
        return sb.toString();
    }

    public static String randomUUID() {
        return UUID.randomUUID().toString();
    }

    public static String randomUUIDShort() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 12);
    }

    // ==================== Masking ====================

    public static String maskEmail(String email) {
        if (isBlank(email) || !email.contains("@")) return email;
        String[] parts = email.split("@");
        String name = parts[0];
        String domain = parts[1];

        if (name.length() <= 2) {
            return name.charAt(0) + "***@" + domain;
        }
        return name.charAt(0) + "***" + name.charAt(name.length() - 1) + "@" + domain;
    }

    public static String maskPhone(String phone) {
        if (isBlank(phone) || phone.length() < 4) return phone;
        int visibleDigits = 4;
        String visible = phone.substring(phone.length() - visibleDigits);
        String masked = "*".repeat(phone.length() - visibleDigits);
        return masked + visible;
    }

    public static String maskCardNumber(String cardNumber) {
        if (isBlank(cardNumber) || cardNumber.length() < 4) return cardNumber;
        String last4 = cardNumber.substring(cardNumber.length() - 4);
        return "**** **** **** " + last4;
    }

    // ==================== Truncation ====================

    public static String truncate(String str, int maxLength) {
        if (str == null || str.length() <= maxLength) return str;
        return str.substring(0, maxLength);
    }

    public static String truncateWithEllipsis(String str, int maxLength) {
        if (str == null || str.length() <= maxLength) return str;
        return str.substring(0, maxLength - 3) + "...";
    }

    // ==================== Validation Helpers ====================

    public static boolean containsIgnoreCase(String str, String searchStr) {
        if (str == null || searchStr == null) return false;
        return str.toLowerCase().contains(searchStr.toLowerCase());
    }

    public static boolean equalsIgnoreCase(String str1, String str2) {
        if (str1 == null && str2 == null) return true;
        if (str1 == null || str2 == null) return false;
        return str1.equalsIgnoreCase(str2);
    }

    public static int countOccurrences(String str, String sub) {
        if (isEmpty(str) || isEmpty(sub)) return 0;
        int count = 0;
        int idx = 0;
        while ((idx = str.indexOf(sub, idx)) != -1) {
            count++;
            idx += sub.length();
        }
        return count;
    }

    // ==================== Padding ====================

    public static String padLeft(String str, int length, char padChar) {
        if (str == null) str = "";
        if (str.length() >= length) return str;
        return String.valueOf(padChar).repeat(length - str.length()) + str;
    }

    public static String padRight(String str, int length, char padChar) {
        if (str == null) str = "";
        if (str.length() >= length) return str;
        return str + String.valueOf(padChar).repeat(length - str.length());
    }

    public static String padZero(int number, int length) {
        return padLeft(String.valueOf(number), length, '0');
    }
}
