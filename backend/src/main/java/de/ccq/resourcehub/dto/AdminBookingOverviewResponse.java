package de.ccq.resourcehub.dto;

import de.ccq.resourcehub.entity.BookingStatus;
import java.time.Instant;
import java.time.LocalDate;

public record AdminBookingOverviewResponse(
        Long id,
        Long resourceId,
        Long userId,
        LocalDate startDate,
        LocalDate endDate,
        BookingStatus status,
        Instant createdAt,
        Instant approvedAt,
        Instant rejectedAt,
        String rejectionReason) {
}
