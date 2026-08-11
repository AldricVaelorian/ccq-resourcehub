package de.ccq.resourcehub.controller;

import de.ccq.resourcehub.dto.ApiErrorResponse;
import de.ccq.resourcehub.exception.BookingApprovalException;
import de.ccq.resourcehub.exception.BookingRejectionException;
import de.ccq.resourcehub.service.AvailabilityRuleService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(MethodArgumentNotValidException exception) {
        String message = exception.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .orElse("Request validation failed");
        return ResponseEntity.badRequest().body(new ApiErrorResponse("VALIDATION_FAILED", message));
    }

    @ExceptionHandler(AvailabilityRuleService.InvalidTimeWindowException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidTimeWindow(
            AvailabilityRuleService.InvalidTimeWindowException exception) {
        return ResponseEntity.badRequest()
                .body(new ApiErrorResponse("INVALID_TIME_WINDOW", exception.getMessage()));
    }

    @ExceptionHandler(AvailabilityRuleService.NotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNotFound(AvailabilityRuleService.NotFoundException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiErrorResponse("AVAILABILITY_RULE_NOT_FOUND", exception.getMessage()));
    }

    @ExceptionHandler(BookingApprovalException.class)
    public ResponseEntity<ApiErrorResponse> handleBookingApproval(BookingApprovalException exception) {
        HttpStatus status = switch (exception.getReason()) {
            case NOT_FOUND -> HttpStatus.NOT_FOUND;
            case FORBIDDEN -> HttpStatus.FORBIDDEN;
            case CONFLICT -> HttpStatus.CONFLICT;
        };
        return ResponseEntity.status(status)
                .body(new ApiErrorResponse("BOOKING_APPROVAL_" + exception.getReason(), exception.getMessage()));
    }

    @ExceptionHandler(BookingRejectionException.class)
    public ResponseEntity<ApiErrorResponse> handleBookingRejection(BookingRejectionException exception) {
        HttpStatus status = switch (exception.getReason()) {
            case NOT_FOUND -> HttpStatus.NOT_FOUND;
            case FORBIDDEN -> HttpStatus.FORBIDDEN;
            case CONFLICT -> HttpStatus.CONFLICT;
        };
        return ResponseEntity.status(status)
                .body(new ApiErrorResponse("BOOKING_REJECTION_" + exception.getReason(), exception.getMessage()));
    }
}
