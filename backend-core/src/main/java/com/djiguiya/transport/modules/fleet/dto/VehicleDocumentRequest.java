package com.djiguiya.transport.modules.fleet.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/** Upsert payload for a single document type on a vehicle. */
public record VehicleDocumentRequest(
		@NotBlank @Size(max = 40) String docType,
		@NotNull LocalDate expiresOn,
		LocalDate lastRenewedOn,
		Integer renewalPeriodMonths,
		Long lastAmount,
		String notes
) {
}
