package de.ccq.resourcehub.controller;

import de.ccq.resourcehub.dto.BookingExportResponse;
import de.ccq.resourcehub.exception.BookingExportException;
import de.ccq.resourcehub.service.BookingExportService;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/admin/bookings/export")
public class BookingExportController {

    private final BookingExportService bookingExportService;

    public BookingExportController(BookingExportService bookingExportService) {
        this.bookingExportService = bookingExportService;
    }

    @GetMapping
    public ResponseEntity<List<BookingExportResponse>> exportAllBookings(
            @RequestParam @Positive Long adminId) {
        if (adminId == null || adminId <= 0) {
            throw new de.ccq.resourcehub.exception.AdminBookingOverviewValidationException("adminId must be positive");
        }
        List<BookingExportResponse> bookings = bookingExportService.exportAllBookings(adminId);
        return ResponseEntity.ok(bookings);
    }

    @GetMapping("/status")
    public ResponseEntity<List<BookingExportResponse>> exportBookingsByStatus(
            @RequestParam @Positive Long adminId,
            @RequestParam String status) {
        if (adminId == null || adminId <= 0) {
            throw new de.ccq.resourcehub.exception.AdminBookingOverviewValidationException("adminId must be positive");
        }
        if (status == null || status.trim().isEmpty() || "null".equalsIgnoreCase(status)) {
            throw new de.ccq.resourcehub.exception.AdminBookingOverviewValidationException("status: must not be null or empty");
        }
        List<BookingExportResponse> bookings = bookingExportService.exportBookingsByStatus(adminId, status);
        return ResponseEntity.ok(bookings);
    }

    @GetMapping("/daterange")
    public ResponseEntity<List<BookingExportResponse>> exportBookingsByDateRange(
            @RequestParam @Positive Long adminId,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate) {
        if (adminId == null || adminId <= 0) {
            throw new de.ccq.resourcehub.exception.AdminBookingOverviewValidationException("adminId must be positive");
        }
        List<BookingExportResponse> bookings = bookingExportService.exportBookingsByDateRange(adminId, startDate, endDate);
        return ResponseEntity.ok(bookings);
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(BookingExportException.class)
    public ResponseEntity<de.ccq.resourcehub.dto.ApiErrorResponse> handleBookingExportException(BookingExportException ex) {
        de.ccq.resourcehub.dto.ApiErrorResponse error = new de.ccq.resourcehub.dto.ApiErrorResponse(
                "BOOKING_EXPORT_" + ex.getReason(),
                ex.getMessage());
        return ResponseEntity.status(switch (ex.getReason()) {
                    case NOT_FOUND -> org.springframework.http.HttpStatus.NOT_FOUND;
                    case FORBIDDEN -> org.springframework.http.HttpStatus.FORBIDDEN;
                    case INVALID_REQUEST -> org.springframework.http.HttpStatus.BAD_REQUEST;
                })
                .body(error);
    }
}