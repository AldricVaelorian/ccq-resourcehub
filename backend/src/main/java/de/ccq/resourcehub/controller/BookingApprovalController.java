package de.ccq.resourcehub.controller;

import de.ccq.resourcehub.dto.BookingApprovalRequest;
import de.ccq.resourcehub.dto.BookingResponse;
import de.ccq.resourcehub.service.BookingApprovalService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/bookings")
public class BookingApprovalController {

    private final BookingApprovalService bookingApprovalService;

    public BookingApprovalController(BookingApprovalService bookingApprovalService) {
        this.bookingApprovalService = bookingApprovalService;
    }

    @PutMapping("/{bookingId}/approve")
    public ResponseEntity<BookingResponse> approveBooking(
            @PathVariable Long bookingId, @Valid @RequestBody BookingApprovalRequest request) {
        return ResponseEntity.ok(bookingApprovalService.approveBooking(bookingId, request.managerId()));
    }
}
