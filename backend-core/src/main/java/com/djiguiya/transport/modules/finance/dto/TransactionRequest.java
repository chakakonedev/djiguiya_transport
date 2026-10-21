package com.djiguiya.transport.modules.finance.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record TransactionRequest(
		@NotBlank @Size(max = 20) String type,
		@NotNull LocalDate txnDate,
		@NotNull @Positive Long amount,
		@Size(max = 36) String vehicleId,
		@Size(max = 36) String driverId,
		@Size(max = 36) String tripId,
		@Size(max = 40) String categoryCode,
		@Size(max = 40) String subcategoryCode,
		@Size(max = 255) String label,
		@Size(max = 7) String payrollMonth,
		@Size(max = 20) String payrollKind,
		String notes
) {
}
