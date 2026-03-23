package lk.iit.nextora.common.mapper;

import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * Utility methods for date/time conversions in MapStruct mappers.
 * Use as @Mapper(uses = DateTimeMapper.class)
 */
@Component
public class DateTimeMapper {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    /**
     * Convert LocalDateTime to Date
     */
    public Date toDate(LocalDateTime localDateTime) {
        if (localDateTime == null) {
            return null;
        }
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    /**
     * Convert Date to LocalDateTime
     */
    public LocalDateTime toLocalDateTime(Date date) {
        if (date == null) {
            return null;
        }
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    /**
     * Convert LocalDate to Date
     */
    public Date toDate(LocalDate localDate) {
        if (localDate == null) {
            return null;
        }
        return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    /**
     * Convert Date to LocalDate
     */
    public LocalDate toLocalDate(Date date) {
        if (date == null) {
            return null;
        }
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    /**
     * Convert String to LocalDate
     */
    public LocalDate stringToLocalDate(String dateString) {
        if (dateString == null || dateString.isBlank()) {
            return null;
        }
        return LocalDate.parse(dateString, DATE_FORMATTER);
    }

    /**
     * Convert LocalDate to String
     */
    public String localDateToString(LocalDate localDate) {
        if (localDate == null) {
            return null;
        }
        return localDate.format(DATE_FORMATTER);
    }

    /**
     * Convert String to LocalDateTime
     */
    public LocalDateTime stringToLocalDateTime(String dateTimeString) {
        if (dateTimeString == null || dateTimeString.isBlank()) {
            return null;
        }
        return LocalDateTime.parse(dateTimeString, DATE_TIME_FORMATTER);
    }

    /**
     * Convert LocalDateTime to String
     */
    public String localDateTimeToString(LocalDateTime localDateTime) {
        if (localDateTime == null) {
            return null;
        }
        return localDateTime.format(DATE_TIME_FORMATTER);
    }
}

