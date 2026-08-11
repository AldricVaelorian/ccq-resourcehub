package de.ccq.resourcehub.exception;

public class AdminBookingOverviewException extends RuntimeException {

    private final Reason reason;

    private AdminBookingOverviewException(Reason reason, String message) {
        super(message);
        this.reason = reason;
    }

    public Reason getReason() {
        return reason;
    }

    public static AdminBookingOverviewException adminNotFound(Long adminId) {
        return new AdminBookingOverviewException(Reason.NOT_FOUND, "Administrator not found with ID: " + adminId);
    }

    public static AdminBookingOverviewException forbidden(Long adminId) {
        return new AdminBookingOverviewException(
                Reason.FORBIDDEN, "User " + adminId + " may not view all bookings");
    }

    public enum Reason {
        NOT_FOUND,
        FORBIDDEN
    }
}
