package lk.iit.nextora.common.util;

import lk.iit.nextora.common.exception.custom.BadRequestException;

import java.util.Collection;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Utility class for validation operations.
 * All methods are static - no instantiation needed.
 *
 * Usage:
 * <pre>
 * ValidationUtils.requireNonNull(user, "User");
 * ValidationUtils.requireNonBlank(email, "Email");
 * boolean valid = ValidationUtils.isValidEmail("test@example.com");
 * </pre>
 */
public final class ValidationUtils {

    private ValidationUtils() {
        throw new IllegalStateException("Utility class - cannot be instantiated");
    }

    // Regex patterns
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$"
    );
    private static final Pattern PHONE_PATTERN = Pattern.compile(
            "^\\+?[0-9]{10,15}$"
    );
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
            "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{8,}$"
    );
    private static final Pattern URL_PATTERN = Pattern.compile(
            "^(https?|ftp)://[^\\s/$.?#].[^\\s]*$"
    );
    private static final Pattern ALPHANUMERIC_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9]+$"
    );
    private static final Pattern UUID_PATTERN = Pattern.compile(
            "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$"
    );

    // ==================== Require Methods (throw exception if invalid) ====================

    public static <T> T requireNonNull(T obj, String fieldName) {
        if (obj == null) {
            throw new BadRequestException(fieldName + " is required");
        }
        return obj;
    }

    public static String requireNonBlank(String str, String fieldName) {
        if (str == null || str.isBlank()) {
            throw new BadRequestException(fieldName + " is required and cannot be blank");
        }
        return str;
    }

    public static <T extends Collection<?>> T requireNonEmpty(T collection, String fieldName) {
        if (collection == null || collection.isEmpty()) {
            throw new BadRequestException(fieldName + " is required and cannot be empty");
        }
        return collection;
    }

    public static void requireTrue(boolean condition, String message) {
        if (!condition) {
            throw new BadRequestException(message);
        }
    }

    public static void requireFalse(boolean condition, String message) {
        if (condition) {
            throw new BadRequestException(message);
        }
    }

    public static void requirePositive(Number number, String fieldName) {
        requireNonNull(number, fieldName);
        if (number.doubleValue() <= 0) {
            throw new BadRequestException(fieldName + " must be positive");
        }
    }

    public static void requireNonNegative(Number number, String fieldName) {
        requireNonNull(number, fieldName);
        if (number.doubleValue() < 0) {
            throw new BadRequestException(fieldName + " cannot be negative");
        }
    }

    public static void requireRange(Number number, Number min, Number max, String fieldName) {
        requireNonNull(number, fieldName);
        double val = number.doubleValue();
        if (val < min.doubleValue() || val > max.doubleValue()) {
            throw new BadRequestException(fieldName + " must be between " + min + " and " + max);
        }
    }

    public static void requireLength(String str, int min, int max, String fieldName) {
        requireNonNull(str, fieldName);
        int len = str.length();
        if (len < min || len > max) {
            throw new BadRequestException(fieldName + " must be between " + min + " and " + max + " characters");
        }
    }

    public static void requireMinLength(String str, int min, String fieldName) {
        requireNonNull(str, fieldName);
        if (str.length() < min) {
            throw new BadRequestException(fieldName + " must be at least " + min + " characters");
        }
    }

    public static void requireMaxLength(String str, int max, String fieldName) {
        if (str != null && str.length() > max) {
            throw new BadRequestException(fieldName + " cannot exceed " + max + " characters");
        }
    }

    public static void requireValidEmail(String email, String fieldName) {
        requireNonBlank(email, fieldName);
        if (!isValidEmail(email)) {
            throw new BadRequestException(fieldName + " must be a valid email address");
        }
    }

    public static void requireValidPhone(String phone, String fieldName) {
        requireNonBlank(phone, fieldName);
        if (!isValidPhone(phone)) {
            throw new BadRequestException(fieldName + " must be a valid phone number");
        }
    }

    public static void requireValidPassword(String password, String fieldName) {
        requireNonBlank(password, fieldName);
        if (!isValidPassword(password)) {
            throw new BadRequestException(fieldName + " must contain at least 8 characters with uppercase, lowercase, number, and special character");
        }
    }

    // ==================== Validation Methods (return boolean) ====================

    public static boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }

    public static boolean isValidPhone(String phone) {
        if (phone == null) return false;
        String cleaned = phone.replaceAll("[\\s-()]", "");
        return PHONE_PATTERN.matcher(cleaned).matches();
    }

    public static boolean isValidPassword(String password) {
        return password != null && PASSWORD_PATTERN.matcher(password).matches();
    }

    public static boolean isValidUrl(String url) {
        return url != null && URL_PATTERN.matcher(url).matches();
    }

    public static boolean isAlphanumeric(String str) {
        return str != null && ALPHANUMERIC_PATTERN.matcher(str).matches();
    }

    public static boolean isValidUUID(String uuid) {
        return uuid != null && UUID_PATTERN.matcher(uuid).matches();
    }

    public static boolean isNumeric(String str) {
        if (str == null || str.isEmpty()) return false;
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean isPositiveInteger(String str) {
        if (str == null || str.isEmpty()) return false;
        try {
            return Integer.parseInt(str) > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    // ==================== Collection Validation ====================

    public static boolean isEmpty(Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }

    public static boolean isNotEmpty(Collection<?> collection) {
        return !isEmpty(collection);
    }

    public static boolean isEmpty(Map<?, ?> map) {
        return map == null || map.isEmpty();
    }

    public static boolean isNotEmpty(Map<?, ?> map) {
        return !isEmpty(map);
    }

    public static <T> boolean contains(Collection<T> collection, T item) {
        return collection != null && collection.contains(item);
    }

    // ==================== Equals Validation ====================

    public static void requireEquals(Object obj1, Object obj2, String message) {
        if (obj1 == null && obj2 == null) return;
        if (obj1 == null || !obj1.equals(obj2)) {
            throw new BadRequestException(message);
        }
    }

    public static void requireNotEquals(Object obj1, Object obj2, String message) {
        if (obj1 != null && obj1.equals(obj2)) {
            throw new BadRequestException(message);
        }
    }
}
