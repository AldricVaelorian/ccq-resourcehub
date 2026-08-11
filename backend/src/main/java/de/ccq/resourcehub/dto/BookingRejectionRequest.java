package de.ccq.resourcehub.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record BookingRejectionRequest(
        @NotNull @Positive Long managerId,
        @NotBlank @Size(max = 1000) String reason) {
}
