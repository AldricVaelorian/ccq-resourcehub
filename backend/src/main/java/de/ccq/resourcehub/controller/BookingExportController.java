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
        List<BookingExportResponse> bookings = bookingExportService.exportAllBookings(adminId);
        return ResponseEntity.ok(bookings);
    }

    @GetMapping("/status")
    public ResponseEntity<List<BookingExportResponse>> exportBookingsByStatus(
            @RequestParam @Positive Long adminId,
            @RequestParam String status) {
        List<BookingExportResponse> bookings = bookingExportService.exportBookingsByStatus(adminId, status);
        return ResponseEntity.ok(bookings);
    }

    @GetMapping("/daterange")
    public ResponseEntity<List<BookingExportResponse>> exportBookingsByDateRange(
            @RequestParam @Positive Long adminId,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate) {
        List<BookingExportResponse> bookings = bookingExportService.exportBookingsByDateRange(adminId, startDate, endDate);
        return ResponseEntity.ok(bookings);
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(BookingExportException.class)
    public ResponseEntity<String> handleBookingExportException(BookingExportException ex) {
        return ResponseEntity.status(org.springframework.http.HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }
}