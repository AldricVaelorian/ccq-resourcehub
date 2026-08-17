package de.ccq.resourcehub.exception;

import java.time.LocalDate;

public class BookingExportException extends RuntimeException {

    private final Reason reason;

    public enum Reason {
        NOT_FOUND,
        FORBIDDEN,
        INVALID_REQUEST
    }

    public BookingExportException(String message, Reason reason) {
        super(message);
        this.reason = reason;
    }

    public Reason getReason() {
        return reason;
    }

    public static BookingExportException adminNotFound(Long adminId) {
        return new BookingExportException("Admin with ID " + adminId + " not found", Reason.NOT_FOUND);
    }

    public static BookingExportException forbidden(Long adminId) {
        return new BookingExportException("Admin with ID " + adminId + " is not authorized to export bookings", Reason.FORBIDDEN);
    }

    public static BookingExportException invalidStatus(String status) {
        return new BookingExportException("Invalid status: " + status, Reason.INVALID_REQUEST);
    }

    public static BookingExportException invalidDateRange(LocalDate startDate, LocalDate endDate) {
        return new BookingExportException("Invalid date range: start=" + startDate + ", end=" + endDate, Reason.INVALID_REQUEST);
    }
}