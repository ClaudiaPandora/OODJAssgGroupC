package utils;

public class IDGenerator {
    
    public static String generateID(String prefix, int count) {
        return prefix + String.format("%03d", count + 1);
    }
    
    public static String generateAppointmentID(int count) {
        return "A" + String.format("%04d", count + 1);
    }
    
    public static String generatePaymentID(int count) {
        return "P" + String.format("%04d", count + 1);
    }
    
    public static String generateFeedbackID(int count) {
        return "F" + String.format("%04d", count + 1);
    }
    
    public static String generateCommentID(int count) {
        return "C" + String.format("%04d", count + 1);
    }
}