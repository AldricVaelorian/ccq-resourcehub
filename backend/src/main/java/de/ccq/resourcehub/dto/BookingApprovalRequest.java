package de.ccq.resourcehub.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record BookingApprovalRequest(@NotNull @Positive Long managerId) {
}
