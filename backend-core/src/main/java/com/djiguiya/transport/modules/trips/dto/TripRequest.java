package com.djiguiya.transport.modules.trips.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * Input for creating or updating a trip. Omit {@code driverId} to default to the vehicle primary driver.
 */
public record TripRequest(
		@NotBlank @Size(max = 36) String vehicleId,
		@NotNull LocalDate departureDate,
		@NotNull LocalDate returnDate,
		@Size(max = 36) String driverId,
		Boolean replacement,
		@Size(max = 255) String replacementReason,
		String notes
) {
}
