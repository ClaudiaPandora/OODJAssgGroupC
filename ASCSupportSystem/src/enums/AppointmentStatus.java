package enums;

public enum AppointmentStatus {
    PENDING,
    ASSIGNED,
    COMPLETED,
    CANCELLED;
    
    public static AppointmentStatus fromString(String text) {
        for (AppointmentStatus status : AppointmentStatus.values()) {
            if (status.name().equalsIgnoreCase(text)) {
                return status;
            }
        }
        return PENDING;
    }
}