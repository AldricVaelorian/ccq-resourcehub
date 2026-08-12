package de.ccq.resourcehub.exception;

import java.time.LocalDate;

public class BookingExportException extends RuntimeException {

    public BookingExportException(String message) {
        super(message);
    }

    public static BookingExportException adminNotFound(Long adminId) {
        return new BookingExportException("Admin with ID " + adminId + " not found");
    }

    public static BookingExportException forbidden(Long adminId) {
        return new BookingExportException("Admin with ID " + adminId + " is not authorized to export bookings");
    }

    public static BookingExportException invalidStatus(String status) {
        return new BookingExportException("Invalid status: " + status);
    }

    public static BookingExportException invalidDateRange(LocalDate startDate, LocalDate endDate) {
        return new BookingExportException("Invalid date range: start=" + startDate + ", end=" + endDate);
    }
}