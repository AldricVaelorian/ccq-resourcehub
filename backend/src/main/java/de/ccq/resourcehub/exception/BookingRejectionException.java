package de.ccq.resourcehub.exception;

import de.ccq.resourcehub.entity.BookingStatus;

public class BookingRejectionException extends RuntimeException {

    private final Reason reason;

    private BookingRejectionException(Reason reason, String message) {
        super(message);
        this.reason = reason;
    }

    public Reason getReason() {
        return reason;
    }

    public static BookingRejectionException bookingNotFound(Long bookingId) {
        return new BookingRejectionException(Reason.NOT_FOUND, "Booking not found with ID: " + bookingId);
    }

    public static BookingRejectionException managerNotFound(Long managerId) {
        return new BookingRejectionException(Reason.NOT_FOUND, "Manager not found with ID: " + managerId);
    }

    public static BookingRejectionException forbidden(Long managerId) {
        return new BookingRejectionException(Reason.FORBIDDEN, "User " + managerId + " may not reject bookings");
    }

    public static BookingRejectionException invalidStatus(BookingStatus status) {
        return new BookingRejectionException(
                Reason.CONFLICT, "Only pending bookings can be rejected; status is " + status);
    }

    public enum Reason {
        NOT_FOUND,
        FORBIDDEN,
        CONFLICT
    }
}
