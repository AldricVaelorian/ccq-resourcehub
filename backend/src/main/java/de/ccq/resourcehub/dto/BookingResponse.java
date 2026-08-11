package de.ccq.resourcehub.dto;

import de.ccq.resourcehub.entity.BookingStatus;
import java.time.Instant;
import java.time.LocalDate;

public record BookingResponse(
        Long id,
        Long resourceId,
        Long userId,
        LocalDate startDate,
        LocalDate endDate,
        BookingStatus status,
        Instant approvedAt) {
}
