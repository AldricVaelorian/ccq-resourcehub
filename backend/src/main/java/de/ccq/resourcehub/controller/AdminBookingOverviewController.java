package de.ccq.resourcehub.controller;

import de.ccq.resourcehub.dto.AdminBookingOverviewResponse;
import de.ccq.resourcehub.exception.AdminBookingOverviewValidationException;
import de.ccq.resourcehub.service.AdminBookingOverviewService;
import jakarta.validation.constraints.Positive;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/admin/bookings")
public class AdminBookingOverviewController {

    private final AdminBookingOverviewService adminBookingOverviewService;

    public AdminBookingOverviewController(AdminBookingOverviewService adminBookingOverviewService) {
        this.adminBookingOverviewService = adminBookingOverviewService;
    }

    @GetMapping
    public ResponseEntity<List<AdminBookingOverviewResponse>> findAllBookings(
            @RequestParam @Positive Long adminId) {
        if (adminId == null || adminId <= 0) {
            throw new AdminBookingOverviewValidationException("adminId must be positive");
        }
        return ResponseEntity.ok(adminBookingOverviewService.findAllBookings(adminId));
    }
}
