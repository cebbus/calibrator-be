package com.cebbus.calibrator.common;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.Date;

public class LocalDateOperations {

    public static final String ISO_DATE_FORMAT = "yyyy-MM-dd";
    public static final String DEFAULT_DATE_FORMAT = "dd.MM.yyyy";

    private LocalDateOperations() {
    }

    public static LocalDate dateToLocalDate(Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    public static LocalDateTime dateToLocalDateTime(Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    public static Date localDateToDate(LocalDate localDate) {
        return localDateToDate(localDate, Boolean.FALSE);
    }

    public static Date localDateToDate(LocalDate localDate, Boolean isPm) {
        Instant instant = localDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
        return Date.from(!Boolean.TRUE.equals(isPm) ? instant : instant.plus(13, ChronoUnit.HOURS));
    }

    public static int daysBetween(LocalDate startDate, LocalDate endDate) {
        return (int) ChronoUnit.DAYS.between(startDate, endDate);
    }

    public static LocalDate objectToLocalDate(Object date) {
        try {
            return stringToLocalDate(date.toString(), DEFAULT_DATE_FORMAT);
        } catch (DateTimeParseException e) {
            return isoStringToLocalDate(date.toString());
        }
    }

    public static LocalDate objectToLocalDate(Object date, String format) {
        return stringToLocalDate(date.toString(), format);
    }

    public static LocalDate isoStringToLocalDate(String date) {
        return stringToLocalDate(date, ISO_DATE_FORMAT);
    }

    public static LocalDate stringToLocalDate(String date, String format) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
        return LocalDate.parse(date, formatter);
    }

    public static boolean isValidDate(String query) {
        return isValidDate(query, DEFAULT_DATE_FORMAT);
    }

    public static boolean isValidDate(String query, String format) {
        boolean valid = true;
        try {
            DateTimeFormatter.ofPattern(format).parse(query);
        } catch (DateTimeParseException e) {
            valid = false;
        }
        return valid;
    }

    public static String defaultToIso(String date) {
        LocalDate localDate = stringToLocalDate(date, DEFAULT_DATE_FORMAT);
        return localDate.format(DateTimeFormatter.ofPattern(ISO_DATE_FORMAT));
    }
}
