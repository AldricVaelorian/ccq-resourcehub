package de.ccq.resourcehub.controller;

import de.ccq.resourcehub.dto.BookingRejectionRequest;
import de.ccq.resourcehub.dto.BookingRejectionResponse;
import de.ccq.resourcehub.service.BookingRejectionService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/bookings")
public class BookingRejectionController {

    private final BookingRejectionService bookingRejectionService;

    public BookingRejectionController(BookingRejectionService bookingRejectionService) {
        this.bookingRejectionService = bookingRejectionService;
    }

    @PutMapping("/{bookingId}/reject")
    public ResponseEntity<BookingRejectionResponse> rejectBooking(
            @PathVariable Long bookingId, @Valid @RequestBody BookingRejectionRequest request) {
        return ResponseEntity.ok(
                bookingRejectionService.rejectBooking(bookingId, request.managerId(), request.reason()));
    }
}
