package com.djiguiya.transport.modules.fleet.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

/** Defines a primary driver assignment period for a vehicle; {@code endDate} null means open-ended. */
public record PrimaryDriverRequest(
		@NotBlank String driverId,
		@NotNull LocalDate startDate,
		LocalDate endDate,
		String reason
) {
}
