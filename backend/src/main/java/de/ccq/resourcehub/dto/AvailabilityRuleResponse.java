package de.ccq.resourcehub.dto;

import java.time.DayOfWeek;
import java.time.LocalTime;

public record AvailabilityRuleResponse(
        Long id,
        String name,
        DayOfWeek dayOfWeek,
        LocalTime startTime,
        LocalTime endTime,
        boolean active) {
}
