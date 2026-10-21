package com.djiguiya.transport.modules.finance.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record PayrollPaymentRequest(
		@NotBlank @Size(max = 36) String staffId,
		@NotNull @Positive Long amount,
		@NotBlank @Size(max = 7) String payrollMonth,
		@NotNull LocalDate txnDate,
		@Size(max = 255) String label,
		String notes
) {
}
