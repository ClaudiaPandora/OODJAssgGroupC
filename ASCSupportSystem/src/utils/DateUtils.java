package utils;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class DateUtils {
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    
    public static String getCurrentDate() {
        return LocalDate.now().format(DATE_FORMATTER);
    }
    
    public static String getCurrentTime() {
        return LocalTime.now().format(TIME_FORMATTER);
    }
    
    public static String getCurrentDateTime() {
        return LocalDate.now().format(DATE_FORMATTER) + " " + LocalTime.now().format(TIME_FORMATTER);
    }
    
    public static String formatDate(LocalDate date) {
        return date.format(DATE_FORMATTER);
    }
    
    public static LocalDate parseDate(String dateStr) {
        return LocalDate.parse(dateStr, DATE_FORMATTER);
    }
    
    public static boolean isWithinLast7Days(String dateStr) {
        try {
            java.time.LocalDate date = java.time.LocalDate.parse(dateStr);
            java.time.LocalDate today = java.time.LocalDate.now();
            java.time.LocalDate sevenDaysAgo = today.minusDays(7);

            return (!date.isBefore(sevenDaysAgo)) && (!date.isAfter(today));
        } catch (Exception e) {
            return false;
        }
    }
}