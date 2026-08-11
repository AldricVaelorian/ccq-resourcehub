package de.ccq.resourcehub.exception;

import de.ccq.resourcehub.entity.BookingStatus;

public class BookingApprovalException extends RuntimeException {

    private final Reason reason;

    private BookingApprovalException(Reason reason, String message) {
        super(message);
        this.reason = reason;
    }

    public Reason getReason() {
        return reason;
    }

    public static BookingApprovalException notFound(Long bookingId) {
        return new BookingApprovalException(Reason.NOT_FOUND, "Booking not found with ID: " + bookingId);
    }

    public static BookingApprovalException managerNotFound(Long managerId) {
        return new BookingApprovalException(Reason.NOT_FOUND, "Manager not found with ID: " + managerId);
    }

    public static BookingApprovalException forbidden(Long managerId) {
        return new BookingApprovalException(Reason.FORBIDDEN, "User " + managerId + " may not approve bookings");
    }

    public static BookingApprovalException invalidStatus(BookingStatus status) {
        return new BookingApprovalException(Reason.CONFLICT, "Only pending bookings can be approved; status is " + status);
    }

    public static BookingApprovalException overlap() {
        return new BookingApprovalException(Reason.CONFLICT, "Booking overlaps an already approved booking");
    }

    public enum Reason {
        NOT_FOUND,
        FORBIDDEN,
        CONFLICT
    }
}
