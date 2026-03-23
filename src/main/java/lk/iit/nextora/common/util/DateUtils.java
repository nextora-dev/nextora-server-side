package lk.iit.nextora.common.util;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Date;

/**
 * Utility class for date and time operations.
 * All methods are static - no instantiation needed.
 *
 * Usage:
 * <pre>
 * LocalDateTime now = DateUtils.now();
 * String formatted = DateUtils.format(now, "yyyy-MM-dd");
 * boolean isExpired = DateUtils.isExpired(expiryDate);
 * </pre>
 */
public final class DateUtils {

    private DateUtils() {
        throw new IllegalStateException("Utility class - cannot be instantiated");
    }

    // Common formatters
    public static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    public static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");
    public static final DateTimeFormatter DATETIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    public static final DateTimeFormatter ISO_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    // ==================== Current Time ====================

    public static LocalDateTime now() {
        return LocalDateTime.now();
    }

    public static LocalDate today() {
        return LocalDate.now();
    }

    public static Instant nowInstant() {
        return Instant.now();
    }

    // ==================== Formatting ====================

    public static String format(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(DATETIME_FORMAT) : null;
    }

    public static String format(LocalDateTime dateTime, String pattern) {
        return dateTime != null ? dateTime.format(DateTimeFormatter.ofPattern(pattern)) : null;
    }

    public static String format(LocalDate date) {
        return date != null ? date.format(DATE_FORMAT) : null;
    }

    public static String format(LocalDate date, String pattern) {
        return date != null ? date.format(DateTimeFormatter.ofPattern(pattern)) : null;
    }

    // ==================== Parsing ====================

    public static LocalDateTime parseDateTime(String dateTimeStr) {
        return dateTimeStr != null ? LocalDateTime.parse(dateTimeStr, DATETIME_FORMAT) : null;
    }

    public static LocalDateTime parseDateTime(String dateTimeStr, String pattern) {
        return dateTimeStr != null ? LocalDateTime.parse(dateTimeStr, DateTimeFormatter.ofPattern(pattern)) : null;
    }

    public static LocalDate parseDate(String dateStr) {
        return dateStr != null ? LocalDate.parse(dateStr, DATE_FORMAT) : null;
    }

    public static LocalDate parseDate(String dateStr, String pattern) {
        return dateStr != null ? LocalDate.parse(dateStr, DateTimeFormatter.ofPattern(pattern)) : null;
    }

    // ==================== Conversion ====================

    public static Date toDate(LocalDateTime localDateTime) {
        if (localDateTime == null) return null;
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    public static Date toDate(LocalDate localDate) {
        if (localDate == null) return null;
        return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    public static LocalDateTime toLocalDateTime(Date date) {
        if (date == null) return null;
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    public static LocalDate toLocalDate(Date date) {
        if (date == null) return null;
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    public static long toEpochMilli(LocalDateTime dateTime) {
        return dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    public static LocalDateTime fromEpochMilli(long epochMilli) {
        return Instant.ofEpochMilli(epochMilli).atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    // ==================== Comparison ====================

    public static boolean isExpired(LocalDateTime dateTime) {
        return dateTime != null && dateTime.isBefore(LocalDateTime.now());
    }

    public static boolean isExpired(Date date) {
        return date != null && date.before(new Date());
    }

    public static boolean isFuture(LocalDateTime dateTime) {
        return dateTime != null && dateTime.isAfter(LocalDateTime.now());
    }

    public static boolean isPast(LocalDateTime dateTime) {
        return dateTime != null && dateTime.isBefore(LocalDateTime.now());
    }

    public static boolean isToday(LocalDate date) {
        return date != null && date.equals(LocalDate.now());
    }

    public static boolean isBetween(LocalDateTime dateTime, LocalDateTime start, LocalDateTime end) {
        return dateTime != null && !dateTime.isBefore(start) && !dateTime.isAfter(end);
    }

    // ==================== Calculation ====================

    public static long daysBetween(LocalDate start, LocalDate end) {
        return ChronoUnit.DAYS.between(start, end);
    }

    public static long hoursBetween(LocalDateTime start, LocalDateTime end) {
        return ChronoUnit.HOURS.between(start, end);
    }

    public static long minutesBetween(LocalDateTime start, LocalDateTime end) {
        return ChronoUnit.MINUTES.between(start, end);
    }

    public static long secondsBetween(LocalDateTime start, LocalDateTime end) {
        return ChronoUnit.SECONDS.between(start, end);
    }

    public static LocalDateTime addDays(LocalDateTime dateTime, long days) {
        return dateTime.plusDays(days);
    }

    public static LocalDateTime addHours(LocalDateTime dateTime, long hours) {
        return dateTime.plusHours(hours);
    }

    public static LocalDateTime addMinutes(LocalDateTime dateTime, long minutes) {
        return dateTime.plusMinutes(minutes);
    }

    public static LocalDate addDays(LocalDate date, long days) {
        return date.plusDays(days);
    }

    public static LocalDate addMonths(LocalDate date, long months) {
        return date.plusMonths(months);
    }

    public static LocalDate addYears(LocalDate date, long years) {
        return date.plusYears(years);
    }

    // ==================== Start/End of Day ====================

    public static LocalDateTime startOfDay(LocalDate date) {
        return date.atStartOfDay();
    }

    public static LocalDateTime endOfDay(LocalDate date) {
        return date.atTime(LocalTime.MAX);
    }

    public static LocalDate startOfMonth(LocalDate date) {
        return date.withDayOfMonth(1);
    }

    public static LocalDate endOfMonth(LocalDate date) {
        return date.withDayOfMonth(date.lengthOfMonth());
    }

    public static LocalDate startOfYear(LocalDate date) {
        return date.withDayOfYear(1);
    }

    public static LocalDate endOfYear(LocalDate date) {
        return date.withDayOfYear(date.lengthOfYear());
    }

    // ==================== Readable Format ====================

    public static String timeAgo(LocalDateTime dateTime) {
        if (dateTime == null) return "";

        long seconds = secondsBetween(dateTime, now());

        if (seconds < 60) return seconds + " seconds ago";
        if (seconds < 3600) return (seconds / 60) + " minutes ago";
        if (seconds < 86400) return (seconds / 3600) + " hours ago";
        if (seconds < 2592000) return (seconds / 86400) + " days ago";
        if (seconds < 31536000) return (seconds / 2592000) + " months ago";
        return (seconds / 31536000) + " years ago";
    }
}
