package de.ccq.resourcehub.exception;

public class AdminBookingOverviewValidationException extends RuntimeException {

    public AdminBookingOverviewValidationException(String message) {
        super(message);
    }

    public AdminBookingOverviewValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}